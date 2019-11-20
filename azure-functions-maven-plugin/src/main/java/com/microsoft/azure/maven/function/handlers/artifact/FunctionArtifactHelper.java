/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.maven.function.handlers.artifact;

import com.microsoft.azure.management.appservice.AppSetting;
import com.microsoft.azure.maven.deploytarget.DeployTarget;
import com.microsoft.azure.maven.function.Constants;
import com.microsoft.azure.storage.CloudStorageAccount;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.util.Map;

import static com.microsoft.azure.maven.function.Constants.INTERNAL_STORAGE_KEY;
import static com.microsoft.azure.maven.function.Constants.LOCAL_SETTINGS_FILE;

public class FunctionArtifactHelper {

    private static final String STAGE_DIR_NOT_FOUND = "Azure Functions stage directory not found. " +
            "Please run 'mvn package azure-functions:package' first.";
    private static final String INTERNAL_STORAGE_NOT_FOUND = "Application setting 'AzureWebJobsStorage' not found.";

    public static File createFunctionArtifact(final String stagingDirectoryPath) throws Exception {
        final File stageDirectory = new File(stagingDirectoryPath);
        final File zipPackage = new File(stagingDirectoryPath.concat(Constants.ZIP_EXT));

        if (!stageDirectory.exists()) {
            throw new Exception(STAGE_DIR_NOT_FOUND);
        }

        ZipUtil.pack(stageDirectory, zipPackage);
        ZipUtil.removeEntry(zipPackage, LOCAL_SETTINGS_FILE);

        return zipPackage;
    }

    public static CloudStorageAccount getCloudStorageAccount(final DeployTarget target) throws Exception {
        final Map<String, AppSetting> settingsMap = target.getAppSettings();

        if (settingsMap != null) {
            final AppSetting setting = settingsMap.get(INTERNAL_STORAGE_KEY);
            if (setting != null) {
                final String value = setting.value();
                if (StringUtils.isNotEmpty(value)) {
                    return CloudStorageAccount.parse(value);
                }
            }
        }
        throw new Exception(INTERNAL_STORAGE_NOT_FOUND);
    }
}
