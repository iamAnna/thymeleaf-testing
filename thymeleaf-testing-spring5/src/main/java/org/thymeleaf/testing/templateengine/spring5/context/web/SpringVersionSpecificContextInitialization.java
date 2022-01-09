/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2022, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.thymeleaf.testing.templateengine.spring5.context.web;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.exceptions.ConfigurationException;
import org.thymeleaf.testing.templateengine.util.SpringVersionUtils;
import org.thymeleaf.util.ClassLoaderUtils;
import org.thymeleaf.web.IWebExchange;


final class SpringVersionSpecificContextInitialization {

    private static Logger LOG = LoggerFactory.getLogger(SpringVersionSpecificContextInitialization.class);

    private static final String PACKAGE_NAME = SpringVersionSpecificContextInitialization.class.getPackage().getName();
    private static final String SPRING5_DELEGATE_CLASS = PACKAGE_NAME + ".Spring5VersionSpecificContextInitializer";

    private static final ISpringVersionSpecificContextInitializer spring5Delegate;




    static {

        if (SpringVersionUtils.isSpring50AtLeast()) {

            LOG.trace("[THYMELEAF][TESTING] Spring 5.0+ found on classpath. Initializing testing system for using Spring 5 in tests");

            try {
                final Class<?> implClass = ClassLoaderUtils.loadClass(SPRING5_DELEGATE_CLASS);
                spring5Delegate = (ISpringVersionSpecificContextInitializer) implClass.newInstance();
            } catch (final Exception e) {
                throw new ExceptionInInitializerError(
                        new ConfigurationException(
                                "Environment has been detected to be at least Spring 5, but thymeleaf could not initialize a " +
                                "delegate of class \"" + SPRING5_DELEGATE_CLASS + "\"", e));
            }

        } else {

            throw new ExceptionInInitializerError(
                    new ConfigurationException(
                        "The testing infrastructure could not create initializer for the specific version of Spring being" +
                        "used. Currently Spring 3.0, 3.1, 3.2 and 4.x are supported."));

        }

    }


    static void versionSpecificAdditionalVariableProcessing(
            final ApplicationContext applicationContext, final ConversionService conversionService,
            final IWebExchange exchange, final Map<String,Object> variables) {

        if (spring5Delegate != null) {
            spring5Delegate.versionSpecificAdditionalVariableProcessing(
                    applicationContext, conversionService, exchange, variables);
            return;
        }

        throw new ConfigurationException(
                "The testing infrastructure could not create initializer for the specific version of Spring being" +
                "used. Currently Spring 3.0, 3.1, 3.2, 4.x and 5.x are supported.");

    }



    static IWebContext versionSpecificCreateContextInstance(
            final ApplicationContext applicationContext, final IWebExchange exchange,
            final Locale locale, final Map<String,Object> variables) {

        if (spring5Delegate != null) {
            return spring5Delegate.versionSpecificCreateContextInstance(
                    applicationContext, exchange, locale, variables);
        }

        throw new ConfigurationException(
                "The testing infrastructure could not create initializer for the specific version of Spring being" +
                "used. Currently Spring 3.0, 3.1, 3.2, 4.x and 5.x are supported.");

    }




    private SpringVersionSpecificContextInitialization() {
        super();
    }



    
}