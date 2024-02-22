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

import com.intellij.conversion.*;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * Project converter intended for configuration V1 to V2 format migration.
 *
 * Does not seem to be starting regardless of the things I tried.
 *
 * @author Tomasz Obszarny
 * @since 2.0
 * @version 2.0
 */
public class V2ProjectConverter extends ProjectConverter {
    
    private static final boolean TOGGLE = false;

    private static final Logger LOGGER = Logger.getInstance(V2ProjectConverter.class);

    @Override
    public @Nullable ConversionProcessor<ComponentManagerSettings> createProjectFileConverter() {
        return new ConversionProcessor<ComponentManagerSettings>() {

            @Override
            public boolean isConversionNeeded(ComponentManagerSettings componentManagerSettings) {
                LOGGER.debug("***** isConversionNeeded()");
                return TOGGLE;
            }

            @Override
            public void process(ComponentManagerSettings componentManagerSettings) throws CannotConvertException {
                LOGGER.debug("***** process()");
            }
        };
    }

    @Override
    public @Nullable ConversionProcessor<WorkspaceSettings> createWorkspaceFileConverter() {
        return new ConversionProcessor<WorkspaceSettings>() {

            @Override
            public boolean isConversionNeeded(WorkspaceSettings workspaceSettings) {
                LOGGER.debug("***** isConversionNeeded()");
                return TOGGLE;
            }

            @Override
            public void process(WorkspaceSettings workspaceSettings) throws CannotConvertException {
                LOGGER.debug("***** process()");
            }
        };
    }

    @Override
    public @Nullable ConversionProcessor<RunManagerSettings> createRunConfigurationsConverter() {
        return new ConversionProcessor<RunManagerSettings>() {

            @Override
            public boolean isConversionNeeded(RunManagerSettings runManagerSettings) {
                LOGGER.debug("***** isConversionNeeded()");
                return TOGGLE;
            }

            @Override
            public void process(RunManagerSettings runManagerSettings) throws CannotConvertException {
                LOGGER.debug("***** process()");
            }
        };
    }

    @Override
    public @Nullable ConversionProcessor<ModuleSettings> createModuleFileConverter() {
        return new ConversionProcessor<ModuleSettings>() {

            @Override
            public boolean isConversionNeeded(ModuleSettings moduleSettings) {
                LOGGER.debug("***** isConversionNeeded()");
                return TOGGLE;
            }

            @Override
            public void process(ModuleSettings moduleSettings) throws CannotConvertException {
                LOGGER.debug("***** process()");
            }
        };
    }

    @Override
    public @Nullable ConversionProcessor<ProjectLibrariesSettings> createProjectLibrariesConverter() {
        return new ConversionProcessor<ProjectLibrariesSettings>() {

            @Override
            public boolean isConversionNeeded(ProjectLibrariesSettings projectLibrariesSettings) {
                LOGGER.debug("***** isConversionNeeded()");
                return TOGGLE;
            }

            @Override
            public void process(ProjectLibrariesSettings projectLibrariesSettings) throws CannotConvertException {
                LOGGER.debug("***** process()");
            }
        };
    }

    @Override
    public @Nullable ConversionProcessor<ArtifactsSettings> createArtifactsConverter() {
        return new ConversionProcessor<ArtifactsSettings>() {

            @Override
            public boolean isConversionNeeded(ArtifactsSettings artifactsSettings) {
                LOGGER.debug("***** isConversionNeeded()");
                return TOGGLE;
            }

            @Override
            public void process(ArtifactsSettings artifactsSettings) throws CannotConvertException {
                LOGGER.debug("***** process()");
            }
        };
    }
}
