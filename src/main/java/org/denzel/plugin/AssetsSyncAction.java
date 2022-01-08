// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.denzel.plugin;

import org.denzel.plugin.model.AesstsInfo;
import org.denzel.plugin.model.AssetsConfig;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.YAMLFileType;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLDocumentImpl;
import org.jetbrains.yaml.psi.impl.YAMLMappingImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

/**
 * If conditions support it, makes a menu visible to display information about the caret.
 *
 * @see AnAction
 */
public class AssetsSyncAction extends AnAction {

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
        final PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        final boolean isYaml = psiFile.getFileType() instanceof YAMLFileType;
        //Set visibility only in case of existing project and editor
        e.getPresentation().setEnabledAndVisible(project != null && isYaml);
    }

    /**
     * Displays a message with information about the current caret.
     *
     * @param e Event related to this action
     */
    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
        PsiElement rootDoc = psiFile.getFirstChild();
        if (rootDoc instanceof YAMLDocumentImpl) {
            AssetsConfig config = CommonUtil.getAssetsConfig(e.getProject().getBasePath());
            builderDartCode(config);
            WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
                PsiElement newAssets = createAssets(e.getProject(), config.assetsList);
                PsiElement flutterNode = getNodeDefault(rootDoc, "flutter", null);
                getNodeDefault(flutterNode, "assets", newAssets);
            });
        }
    }

    void builderDartCode(AssetsConfig config) {
        if(config.hasMakeDartFile) {
            try {
                File dartFile = CommonUtil.createFile(config.proPath, config.dartFilePath);
                Path path = Paths.get(config.proPath, config.dartFilePath);
                StringBuilder sb = new StringBuilder();
                HashSet<String> names = new HashSet();

                for(AesstsInfo it : config.assetsList) {
                    if(names.contains(it.varName)) {
                        for(int i = 1;true;i++) {
                            if(!names.contains(it.varName + i)) {
                                it.varName += i;
                                break;
                            }
                        }
                    }
                    names.add(it.varName);
                    sb.append(it.toDartCode());
                }
                Files.writeString(path, sb.toString());
                /// 刷新IDE目录
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dartFile).refresh(true, true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    PsiElement createAssets(Project project, List<AesstsInfo> images) {
        final YAMLElementGenerator gen = YAMLElementGenerator.getInstance(project);
        StringBuilder sb = new StringBuilder();
        boolean frist = true;
        for (AesstsInfo it : images) {
            if(!frist) {
                sb.append("\n");
            } else {
                frist = false;
            }
            sb.append("- ");
            sb.append(it.refPath);
        }
        return gen.createYamlKeyValue("assets", sb.toString());
    }

    PsiElement getNodeDefault(PsiElement curNode, String keyText, PsiElement replaceNode) {
        final YAMLElementGenerator gen = YAMLElementGenerator.getInstance(curNode.getProject());
        if (curNode.getLastChild() instanceof YAMLMappingImpl) {
            curNode = curNode.getLastChild();
        }
        PsiElement psiChild = curNode.getFirstChild();
        while (psiChild != null) {
            if (psiChild instanceof YAMLKeyValue && keyText.equals(((YAMLKeyValue) psiChild).getKeyText())) {
                if (replaceNode != null) {
                    psiChild.replace(replaceNode);
                }
                return psiChild;
            }
            psiChild = psiChild.getNextSibling();
        }
        curNode.add(gen.createEol());
        if (replaceNode == null) {
            replaceNode = gen.createYamlKeyValue(keyText, "");
        }
        curNode.add(replaceNode);
        return curNode.getLastChild();
    }

    /**
     * 添加图片引用
     *
     * @param parent
     * @param path
     */
    void addImagePath(PsiElement parent, String path) {
        final YAMLElementGenerator gen = YAMLElementGenerator.getInstance(parent.getProject());
        final PsiElement blockEl = gen.createEol();
        final PsiElement split = PsiTreeUtil.getDeepestFirst(gen.createDummyYamlWithText("-"));
        final PsiElement space = gen.createSpace();
        PsiElement txt = gen.createDummyYamlWithText(path);

        parent.add(blockEl);
        parent.add(split);
        parent.add(space);
        parent.add(txt);
    }
}
