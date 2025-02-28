package core.di.factory;

import core.annotation.Inject;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.function.Predicate.isEqual;
import static org.reflections.ReflectionUtils.getAllConstructors;
import static org.reflections.ReflectionUtils.withAnnotation;

public final class BeanFactoryUtils {

    private BeanFactoryUtils() { }

    /**
     * 인자로 전달하는 클래스의 생성자 중 @Inject 애노테이션이 설정되어 있는 생성자를 반환
     *
     * @param clazz
     * @return
     * @Inject 애노테이션이 설정되어 있는 생성자는 클래스당 하나로 가정한다.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Optional<Constructor> getInjectedConstructor(final Class<?> clazz) {
        return getAllConstructors(clazz, withAnnotation(Inject.class))
                .stream()
                .findFirst();
    }

    /**
     * 인자로 전달되는 클래스의 구현 클래스. 만약 인자로 전달되는 Class가 인터페이스가 아니면 전달되는 인자가 구현 클래스,
     * 인터페이스인 경우 BeanFactory가 관리하는 모든 클래스 중에 인터페이스를 구현하는 클래스를 찾아 반환
     *
     * @param injectedClazz
     * @param pareInstantiateBeans
     * @return
     */
    public static Class<?> findConcreteClass(final Class<?> injectedClazz,
                                             final Set<Class<?>> pareInstantiateBeans) {
        if (!injectedClazz.isInterface()) {
            return injectedClazz;
        }

        return pareInstantiateBeans.stream()
                .filter(isImplementation(injectedClazz))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(injectedClazz + "인터페이스를 구현하는 Bean이 존재하지 않는다."));
    }

    private static Predicate<Class<?>> isImplementation(final Class<?> injectedClazz) {
        return clazz -> Arrays.stream(clazz.getInterfaces())
                .anyMatch(isEqual(injectedClazz));
    }
}
