package com.codermonkeys.textrecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button choose;
    private TextView resultTv;
    private ImageView imageView;

    public static final int PIC_IMAGE = 121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        choose = findViewById(R.id.btn_choose);
        resultTv = findViewById(R.id.text_view);
        imageView = findViewById(R.id.image_view);

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resultTv.setText("");

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Choose Image"), PIC_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PIC_IMAGE && resultCode != 0) {

            imageView.setImageURI(data.getData());

            //create a instance of FirebaseVisionImage
            FirebaseVisionImage image;
            try {
                //get the URI of the image file
                image = FirebaseVisionImage.fromFilePath(getApplicationContext(), data.getData());

                //create FirebaseVisionTextRecognizer instance
                FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                        .getOnDeviceTextRecognizer();

                //Pass the image for processing

                textRecognizer.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText result) {
                                // Task completed successfully

                                String resultText = result.getText();
                                for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                                    String blockText = block.getText();
                                    Float blockConfidence = block.getConfidence();
                                    List<RecognizedLanguage> blockRecognizedLanguages = block.getRecognizedLanguages();
                                    Rect blockFrame = block.getBoundingBox();
                                    for (FirebaseVisionText.Line paragraph : block.getLines()) {
                                        String paragraphText = paragraph.getText();
                                        Float paragraphConfidence = paragraph.getConfidence();
                                        List<RecognizedLanguage> paragraphRecognizedLanguages = paragraph.getRecognizedLanguages();
                                        Rect paragraphFrame = paragraph.getBoundingBox();
                                        for (FirebaseVisionText.Element word : paragraph.getElements()) {
                                            resultTv.append(word.getText() + " ");
                                            String wordText = word.getText();
                                            Float wordConfidence = word.getConfidence();
                                            List<RecognizedLanguage> wordRecognizedLanguages = word.getRecognizedLanguages();
                                            Rect wordFrame = word.getBoundingBox();
                                        }

                                        resultTv.append("\n");
                                    }

                                    resultTv.append("\n\n");
                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...

                                        resultTv.setText("No Text Detected");
                                    }
                                });


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}