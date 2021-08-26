package dev.rikka.tools;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.QualifiedContent.Scope;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInvocation;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings("deprecation")
public class HiddenApiRefineTransform extends Transform {
    @Override
    public String getName() {
        return "HiddenApiRefine";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES);
    }

    @Override
    public Set<? super Scope> getScopes() {
        return Set.of(Scope.PROJECT, Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES);
    }

    @Override
    public Set<? super Scope> getReferencedScopes() {
        return Set.of(Scope.PROJECT, Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES, Scope.PROVIDED_ONLY);
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    private void doTransform(TransformInvocation transform) throws IOException {
        // TODO
    }

    @Override
    public void transform(TransformInvocation transform) throws IOException {
        try {
            doTransform(transform);
        } catch (IOException e) {
            e.printStackTrace();

            throw e;
        }
    }
}
