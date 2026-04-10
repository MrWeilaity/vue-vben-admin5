package com.vben.system.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 启动时校验 Controller 注解规范，不符合要求则直接阻止应用启动。
 */
@Component
@RequiredArgsConstructor
public class ControllerAnnotationStartupValidator implements SmartInitializingSingleton {

    private static final String CONTROLLER_PACKAGE_PREFIX = "com.vben.system.controller";

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void afterSingletonsInstantiated() {
        validate();
    }

    public void validate() {
        List<String> violations = new ArrayList<>();

        requestMappingHandlerMapping.getHandlerMethods().values().stream()
                .map(handlerMethod -> handlerMethod.getBeanType())
                .distinct()
                .sorted(Comparator.comparing(Class::getName))
                .forEach(beanType -> validateControllerClass(beanType, violations));

        if (!violations.isEmpty()) {
            throw new IllegalStateException("Controller 注解校验失败:\n - " + String.join("\n - ", violations));
        }
    }

    private void validateControllerClass(Class<?> beanType, List<String> violations) {
        if (!beanType.getName().startsWith(CONTROLLER_PACKAGE_PREFIX)) {
            return;
        }
        RequestMapping classRequestMapping = AnnotationUtils.findAnnotation(beanType, RequestMapping.class);
        Tag tag = AnnotationUtils.findAnnotation(beanType, Tag.class);

        if (tag == null || !StringUtils.hasText(tag.name())) {
            violations.add(beanType.getName() + " 缺少类级别 @Tag(name=...)");
        }

        if (classRequestMapping == null || !hasAnyPath(classRequestMapping)) {
            violations.add(beanType.getName() + " 缺少类级别 @RequestMapping 且必须声明路径");
        }

        for (Method method : beanType.getDeclaredMethods()) {
            validateMethod(beanType, method, violations);
        }
    }

    private boolean hasAnyPath(RequestMapping requestMapping) {
        return requestMapping.path().length > 0 || requestMapping.value().length > 0;
    }

    private void validateMethod(Class<?> beanType, Method method, List<String> violations) {
        if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic() || method.isBridge()) {
            return;
        }

        RequestMapping directRequestMapping = method.getDeclaredAnnotation(RequestMapping.class);
        boolean hasHttpMapping = hasHttpMethodMappingAnnotation(method);
        Operation operation = AnnotationUtils.findAnnotation(method, Operation.class);
        String methodSignature = beanType.getName() + "#" + method.getName();

        if (directRequestMapping != null) {
            violations.add(methodSignature + " 方法级别不允许使用 @RequestMapping，请改用 @GetMapping/@PostMapping/@PutMapping/@DeleteMapping/@PatchMapping");
        }

        if (hasHttpMapping && (operation == null || !StringUtils.hasText(operation.summary()))) {
            violations.add(methodSignature + " 使用了方法级别 Mapping 注解，必须声明 @Operation(summary=...)");
        }

        if (operation != null && !hasHttpMapping) {
            violations.add(methodSignature + " 使用了 @Operation，但缺少 @GetMapping/@PostMapping/@PutMapping/@DeleteMapping/@PatchMapping");
        }
    }

    private boolean hasHttpMethodMappingAnnotation(Method method) {
        return AnnotationUtils.findAnnotation(method, GetMapping.class) != null
                || AnnotationUtils.findAnnotation(method, PostMapping.class) != null
                || AnnotationUtils.findAnnotation(method, PutMapping.class) != null
                || AnnotationUtils.findAnnotation(method, DeleteMapping.class) != null
                || AnnotationUtils.findAnnotation(method, PatchMapping.class) != null;
    }
}
