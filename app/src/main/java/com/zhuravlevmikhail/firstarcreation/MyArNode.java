package com.zhuravlevmikhail.firstarcreation;

import android.content.Context;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.CompletableFuture;

public class MyArNode extends AnchorNode {

    private AugmentedImage mImage;
    private static CompletableFuture<ModelRenderable> sModelRenderableCompletableFuture;
    private static CompletableFuture<ViewRenderable> sViewRenderableCompletableFuture;

    public MyArNode(Context context, int modelId, int viewId){
        if (sModelRenderableCompletableFuture == null){
            sModelRenderableCompletableFuture = ModelRenderable.builder()
                    .setRegistryId("my_model")
                    .setSource(context, modelId)
                    .build();
        }
        if (sViewRenderableCompletableFuture == null){
            sViewRenderableCompletableFuture = ViewRenderable.builder()
                    .setView(context, viewId)
                    .build();

        }
    }

    public void setImage(AugmentedImage image, ArFragment arFragment){
        if (!sModelRenderableCompletableFuture.isDone()){
            CompletableFuture.allOf(sModelRenderableCompletableFuture)
                    .thenAccept((aVoid)-> {
                        setImage(image, arFragment);
                    }).exceptionally(throwable -> null);
        }
        if (!sViewRenderableCompletableFuture.isDone()){
            CompletableFuture.allOf(sViewRenderableCompletableFuture)
                    .thenAccept(aVoid -> {
                        setImage(image, arFragment);
                    }).exceptionally(throwable -> null);
        }

        setAnchor(mImage.createAnchor(mImage.getCenterPose()));

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());


        node.setParent(this);
        node.setRenderable(sModelRenderableCompletableFuture.getNow(null));
        node.select();


        TransformableNode viewNode = new TransformableNode(arFragment.getTransformationSystem());
        viewNode.setParent(node);
        viewNode.setRenderable(sViewRenderableCompletableFuture.getNow(null));
        viewNode.select();


        arFragment.getArSceneView().getScene().addChild(this);



    }

    public AugmentedImage getImage() {
        return mImage;
    }
}
