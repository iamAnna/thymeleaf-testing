/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
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
package org.thymeleaf.testing.templateengine.engine.resolver;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.AlwaysValidCacheEntryValidity;
import org.thymeleaf.cache.ICacheEntryValidity;
import org.thymeleaf.cache.NonCacheableCacheEntryValidity;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.testing.templateengine.engine.TestExecutor;
import org.thymeleaf.testing.templateengine.exception.TestEngineExecutionException;
import org.thymeleaf.testing.templateengine.resource.ITestResource;
import org.thymeleaf.testing.templateengine.testable.ITest;
import org.thymeleaf.util.PatternSpec;
import org.thymeleaf.util.Validate;







public class TestEngineTemplateResolver implements ITemplateResolver {

    public static final String TEST_TEMPLATE_CONVERSION_CHARSET = "UTF-8";
    
    private static final Logger logger = LoggerFactory.getLogger(TestEngineTemplateResolver.class);


    private String name = this.getClass().getName();
    private Integer order = null;

    private final PatternSpec resolvablePatternSpec = new PatternSpec();
    



    public TestEngineTemplateResolver() {
        super();
    }



    /**
     * <p>
     *   Returns the name of the template resolver
     * </p>
     *
     * @return the name of the template resolver
     */
    public String getName() {
        return this.name;
    }


    /**
     * <p>
     *   Sets a new name for the Template Resolver.
     * </p>
     *
     * @param name the new name
     */
    public void setName(final String name) {
        this.name = name;
    }


    /**
     * <p>
     *   Returns the order in which this template resolver will be asked to resolve
     *   templates as a part of the chain of resolvers configured into the template engine.
     * </p>
     * <p>
     *   Order should start with 1.
     * </p>
     *
     * @return the order in which this template resolver will be called in the chain.
     */
    public Integer getOrder() {
        return this.order;
    }


    /**
     * <p>
     *   Sets a new order for the template engine in the chain. Order should start with 1.
     * </p>
     *
     * @param order the new order.
     */
    public void setOrder(final Integer order) {
        this.order = order;
    }






    public TemplateResolution resolveTemplate(
            final IEngineConfiguration configuration, final IContext context, final String template) {

        Validate.notNull(configuration, "Configuration cannot be null");

        final ITest test = TestExecutor.getThreadTest(); 
        final String testName = TestExecutor.getThreadTestName();

        // Check template mode
        final TemplateMode templateMode = test.getTemplateMode();
        if (templateMode == null) {
            throw new TestEngineExecutionException(
                    "Template mode is null for test \"" + testName + "\", which is forbidden");
        }

        
        // Check input
        final ITestResource input = test.getInput();
        if (input == null) {
            throw new TestEngineExecutionException(
                    "Input is null for test \"" + testName + "\", which is forbidden");
        }
        
        // Organize inputs
        final Map<String,ITestResource> allInputs = new HashMap<String,ITestResource>();
        final Map<String,ITestResource> additionalInputs = test.getAdditionalInputs();
        if (additionalInputs != null) {
            allInputs.putAll(additionalInputs);
        }
        allInputs.put(testName, test.getInput());
        
        // The resource resolver is created instead of reusing one for concurrency reasons 
        final TestEngineResourceResolver resourceResolver = 
                new TestEngineResourceResolver(allInputs, TEST_TEMPLATE_CONVERSION_CHARSET);
        
        // Compute validity according to the "inputCacheable" property established at the test
        final ICacheEntryValidity validity =
                (test.isInputCacheable()?
                        AlwaysValidCacheEntryValidity.INSTANCE :
                        NonCacheableCacheEntryValidity.INSTANCE);
                
        
        
        // Create the template resolution object with all the gathered info
        return new TemplateResolution(
                template,
                template,
                resourceResolver, 
                TEST_TEMPLATE_CONVERSION_CHARSET, 
                templateMode, 
                validity);
        
    }
    
    
}
