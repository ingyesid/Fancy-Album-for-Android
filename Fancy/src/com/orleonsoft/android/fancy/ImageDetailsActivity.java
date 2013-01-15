package com.orleonsoft.android.fancy;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.devspark.appmsg.AppMsg;
import com.orleonsoft.android.fancy.util.Util;

/**
 * File: ImageDetailActivity.java Autor: Yesid Lazaro Mayoriano
 */

public class ImageDetailsActivity extends SherlockActivity {

	private ImageView imageViewPhoto;
	private ProgressBar progressBar;
	private Bitmap bitmap;
	private PhotoViewAttacher photoViewAttacher;
	private Uri imageUri;
	private long _idImage;

	private Intent broadcastIntent;

	private Handler mHandler;
	private Cursor mCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		ColorDrawable color = new ColorDrawable(Color.BLACK);
		color.setAlpha(20);
		getSupportActionBar().setBackgroundDrawable(color);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mHandler = new Handler();

		setContentView(R.layout.activity_image_details);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		imageViewPhoto = (ImageView) findViewById(R.id.img_photo);

		photoViewAttacher = new PhotoViewAttacher(imageViewPhoto);
		photoViewAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View view, float x, float y) {
				// TODO Auto-generated method stub
				getSupportActionBar().show();
				hideActionBarDelayed(mHandler);
			}
		});

		broadcastIntent = new Intent(AppConstants.LOAD_GALLERY_ACTION);
		if (!getIntent().getExtras().isEmpty()
				|| getIntent().getExtras() != null) {
			try {
				_idImage = getIntent().getExtras()
						.getLong(HomeActivity._ID_KEY);

				imageUri = Uri.withAppendedPath(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ""
								+ _idImage);
				new LoadImageTask().execute();

			} catch (Exception e) {
				AppMsg.makeText(ImageDetailsActivity.this,
						"Error loading image ", AppMsg.STYLE_ALERT).show();
			}

		}
	}

	private void hideActionBarDelayed(Handler handler) {
		handler.postDelayed(new Runnable() {
			public void run() {
				getSupportActionBar().hide();
			}
		}, 2000);
	}

	@Override
	public void onResume() {
		super.onResume();
		getSupportActionBar().show();
		hideActionBarDelayed(mHandler);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.activity_image_details, menu);
		MenuItem actionItem = menu.findItem(R.id.action_share);
		ShareActionProvider actionProvider = (ShareActionProvider) actionItem
				.getActionProvider();
		actionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

		actionProvider.setShareIntent(createShareIntent());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			goHome();

			break;
		case R.id.action_delete_img:
			new DeleteImageTask().execute();
			break;
		case R.id.action_rotate_right:
			rotateBitmap(90);
			break;

		case R.id.action_rotate_left:
			rotateBitmap(-90);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void goHome() {
		Intent intent = new Intent(ImageDetailsActivity.this,
				HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	public boolean deleteImage(Uri uriImage) {
		boolean result = false;
		int numRows = getContentResolver().delete(imageUri, null, null);
		if (numRows == 1) {
			lauchFileScan();
			result = true;
		}
		return result;

	}

	private void lauchFileScan() {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		mediaScanIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		ImageDetailsActivity.this.sendBroadcast(mediaScanIntent);
	}

	private Intent createShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("image/*");
		shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
		return shareIntent;
	}

	public void rotateBitmap(int grados) {
		bitmap = Util.rotateBitmap(bitmap, grados, true);
		imageViewPhoto.setImageBitmap(bitmap);
		photoViewAttacher.update();
	}

	class LoadImageTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Boolean isThereError = false;
			if (isCancelled()) {
				ImageDetailsActivity.this.runOnUiThread(new Runnable() {

					public void run() {
						if (progressBar != null) {
							progressBar.setVisibility(View.GONE);
						}
					}
				});

			}
			try {
				bitmap = MediaStore.Images.Media.getBitmap(
						getContentResolver(), imageUri);
				mCursor=MediaStore.Images.Media.query(getContentResolver(), imageUri, null);
			
			
				for (int i = 0; i < mCursor.getColumnCount(); i++) {
					System.out.println(mCursor.getColumnName(i));
				}
				

			} catch (Exception e) {
				isThereError = true;
			}

			return isThereError;
		}

		@Override
		protected void onPostExecute(Boolean isThereError) {
			// TODO Auto-generated method stub
			super.onPostExecute(isThereError);
			progressBar.setVisibility(View.GONE);
			if (isThereError) {
				AppMsg.makeText(ImageDetailsActivity.this,
						"Error loading image,try again ", AppMsg.STYLE_ALERT)
						.show();

			} else {
				imageViewPhoto.setVisibility(View.VISIBLE);
				imageViewPhoto.setImageBitmap(bitmap);
				photoViewAttacher.update();

			}

		}

	}

	class DeleteImageTask extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog = new ProgressDialog(ImageDetailsActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage("deleting image ...");
			progressDialog.show();

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Boolean isThereError = false;
			if (isCancelled()) {
				ImageDetailsActivity.this.runOnUiThread(new Runnable() {

					public void run() {
						if (progressDialog != null) {
							progressDialog.dismiss();
						}
					}
				});

			}
			try {
				if (!deleteImage(imageUri)) {
					isThereError = true;
				}
			} catch (Exception e) {
				isThereError = true;
			}

			return isThereError;
		}

		@Override
		protected void onPostExecute(Boolean isThereError) {
			// TODO Auto-generated method stub
			super.onPostExecute(isThereError);
			progressDialog.dismiss();
			if (isThereError) {
				AppMsg.makeText(ImageDetailsActivity.this,
						"Error deleting image try again", AppMsg.STYLE_ALERT)
						.show();
			} else {
				goHome();
				sendBroadcast(broadcastIntent);
			}

		}

	}

}
