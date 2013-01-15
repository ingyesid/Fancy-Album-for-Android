package com.orleonsoft.android.fancy.util;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * File: Util.java Autor: Yesid Lazaro Mayoriano
 */

public class Util {

	public static Bitmap rotateBitmap(Bitmap bitmapOriginal, int grados,
			boolean recycle) {
		if (grados != 0) {
			int width = bitmapOriginal.getWidth();
			int height = bitmapOriginal.getHeight();
			Matrix matrix = new Matrix();
			matrix.postRotate((float) grados);
			Bitmap result = Bitmap.createBitmap(bitmapOriginal, 0, 0, width,
					height, matrix, true);
			if (recycle) {
				bitmapOriginal.recycle();
			}

			bitmapOriginal = result;
		}

		return bitmapOriginal;
	}

	public static Bitmap compressBitmap(Bitmap bitmapOriginal) {

		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		bitmapOriginal.compress(CompressFormat.JPEG, 90, arrayOutputStream);
		byte[] data = arrayOutputStream.toByteArray();
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		return bitmap;
	}

}
