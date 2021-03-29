/*******************************************************************************
 * Copyright (c) 2020 the qc-atlas contributors.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.planqk.atlas.web.annotation;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Special implementation of RequestMappingHandlerMapping that optionally adds version suffixes to controller URLs.
 */
public class VersionedRequestHandlerMapping extends RequestMappingHandlerMapping {
    /**
     * Utility function to manually add routes from a given handler instance.
     * <p>
     * Only for testing!
     */
    public void populateFromHandler(Object handler) {
        detectHandlerMethods(handler);
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (info != null) {
            final ApiVersion methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, ApiVersion.class);
            if (methodAnnotation != null) {
                // Prepend our version mapping to the real method mapping.
                info = createApiVersionInfo(methodAnnotation).combine(info);
            }

            final ApiVersion typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(handlerType, ApiVersion.class);
            if (methodAnnotation == null && typeAnnotation != null) {
                // Append our version mapping to the real controller mapping.
                info = createApiVersionInfo(typeAnnotation).combine(info);
            }
        }
        return info;
    }

    private RequestMappingInfo createApiVersionInfo(ApiVersion annotation) {
        return new RequestMappingInfo(
            new PatternsRequestCondition(annotation.value(), getUrlPathHelper(), getPathMatcher(),
                useSuffixPatternMatch(), useTrailingSlashMatch(), getFileExtensions()),
            null, null, null, null, null, null);
    }
}