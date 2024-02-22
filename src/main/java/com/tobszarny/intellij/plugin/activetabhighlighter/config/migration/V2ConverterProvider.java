/*
 *
 *  Copyright (c) 2024 Tomasz Obszarny
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tobszarny.intellij.plugin.activetabhighlighter.config.migration;

import com.intellij.conversion.ConversionContext;
import com.intellij.conversion.ConverterProvider;
import com.intellij.conversion.ProjectConverter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;


public class V2ConverterProvider extends ConverterProvider {

    private static final Logger LOGGER = Logger.getInstance(V2ConverterProvider.class);

    public V2ConverterProvider() {
        LOGGER.debug("***** V2ConverterProvider() ");
    }

    @Override
    public @NlsContexts.DialogMessage @NotNull String getConversionDescription() {
        return "Do the V2?";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull ProjectConverter createConverter(@NotNull ConversionContext conversionContext) {
        return new V2ProjectConverter();
    }
}
