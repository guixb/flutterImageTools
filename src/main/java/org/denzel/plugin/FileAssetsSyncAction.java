// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.denzel.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLFileType;

/**
 * If conditions support it, makes a menu visible to display information about the caret.
 *
 * @see AnAction
 */
public class FileAssetsSyncAction extends AssetsSyncAction {

    /**
     * Sets visibility and enables this action menu item if:
     * <ul>
     *   <li>a project is open</li>
     *   <li>an editor is active</li>
     * </ul>
     *
     * @param e Event related to this action
     */
    @Override
    public void update(@NotNull final AnActionEvent e) {
        // Get required data keys
        final Project project = e.getProject();
        final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        final boolean isYaml = psiFile != null &&psiFile.getFileType() instanceof YAMLFileType;
        //Set visibility only in case of existing project and editor
        e.getPresentation().setEnabledAndVisible(project != null && isYaml);
    }


}
