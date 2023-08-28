package com.example.tagitapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerator {

    public static void generatePdfFromTableLayout(Context context, TableLayout tableLayout, String fileName, int pageNumber, int padding) {
        File pdfFile = new File(context.getExternalFilesDir(null), getFileNameWithPageNumber(fileName, pageNumber));

        int totalWidth = tableLayout.getWidth() + 2 * padding; // Add padding to width
        int totalHeight = tableLayout.getHeight() + 2 * padding; // Add padding to height

        Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // Fill the canvas with white background

        // Translate the canvas to account for padding
        canvas.translate(padding, padding);
        tableLayout.draw(canvas);

        try {
            FileOutputStream outputStream = new FileOutputStream(pdfFile);
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(totalWidth, totalHeight, pageNumber).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            canvas = page.getCanvas();
            canvas.drawColor(Color.WHITE); // Fill the page with white background

            // Translate the canvas to account for padding
            canvas.translate(padding, padding);
            canvas.drawBitmap(bitmap, 0, 0, null);

            document.finishPage(page);
            document.writeTo(outputStream);
            outputStream.close();
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        openPdfWithExternalApp(context, pdfFile);
    }


    private static String getFileNameWithPageNumber(String fileName, int pageNumber) {
        return fileName.replace(".pdf", "_" + pageNumber + ".pdf");
    }

    private static void openPdfWithExternalApp(Context context, File pdfFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(pdfFile);
        }
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No PDF viewer app installed", Toast.LENGTH_SHORT).show();
        }
    }
}
