package com.orleonsoft.android.fancy;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.appmsg.AppMsg;

public class HomeActivity extends SherlockActivity implements
		OnItemClickListener, OnItemLongClickListener {

	private static final int ACTION_TAKE_PHOTO = 1;
	public static String _ID_KEY = "_id";
	private String mCurrentPhotoPath;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	private LayoutInflater mLayoutInflater;
	private Cursor galleryCursor;
	private GridView gridPhotos;
	private AdapterGridPhotos adapterGridPhotos;
	private ActionMode mMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		gridPhotos = (GridView) findViewById(R.id.grid_photos);
		gridPhotos.setOnItemClickListener(this);
		gridPhotos.setOnItemLongClickListener(this);
		mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		new LoadPhotoAlbumTask().execute();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		registerReceiver(broadcastReceiver, new IntentFilter(
				AppConstants.LOAD_GALLERY_ACTION));

	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("FANCY", "RECEIVER");
			new LoadPhotoAlbumTask().execute();
		}
	};

	/**
	 * launch image capture intent
	 * 
	 * @param actionCode
	 */
	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		File file = null;
		try {
			file = setUpPhotoFile();
			mCurrentPhotoPath = file.getAbsolutePath();
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(file));
		} catch (IOException e) {
			e.printStackTrace();
			file = null;
			mCurrentPhotoPath = null;
		}
		startActivityForResult(takePictureIntent, actionCode);
	}

	private void handleCameraPhoto(Intent intent) {
		if (mCurrentPhotoPath != null) {
			galleryAddPic();
			mCurrentPhotoPath = null;
			new Handler().postDelayed(new Runnable() {
				public void run() {
					sendBroadcast(new Intent(AppConstants.LOAD_GALLERY_ACTION));
				};
				// wait 3 sec to load again the gallery
			}, 3000);
		}

	}

	/**
	 * Photo album for this application
	 **/
	private String getAlbumName() {
		return getString(R.string.app_name);
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory
					.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		return imageF;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private File setUpPhotoFile() throws IOException {

		File file = createImageFile();
		mCurrentPhotoPath = file.getAbsolutePath();
		return file;
	}

	/**
	 * add picture to gallery from file
	 */
	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		HomeActivity.this.sendBroadcast(mediaScanIntent);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_TAKE_PHOTO:
			if (resultCode == RESULT_OK) {
				handleCameraPhoto(data);
				AppMsg.makeText(HomeActivity.this,
						"Picture Captured sucesfully", AppMsg.STYLE_CONFIRM)
						.show();

			}
			if (resultCode == RESULT_CANCELED) {
				AppMsg.makeText(HomeActivity.this,
						"Picture Captured was canceled", AppMsg.STYLE_ALERT)
						.show();

			}

			break;

		default:
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_about) {
			startActivity(new Intent(HomeActivity.this, AboutActivity.class));
		}
		if (item.getItemId() == R.id.action_take_photo) {

			dispatchTakePictureIntent(ACTION_TAKE_PHOTO);

		}
		if (item.getItemId() == R.id.action_refresh) {

			new LoadPhotoAlbumTask().execute();

		}
		return super.onOptionsItemSelected(item);

	}

	class LoadPhotoAlbumTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			mProgressDialog = new ProgressDialog(HomeActivity.this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setMessage("Loading pictures ...");
			mProgressDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean isThereAnError = false;
			if (isCancelled()) {
				HomeActivity.this.runOnUiThread(new Runnable() {

					public void run() {
						if (mProgressDialog != null) {
							mProgressDialog.dismiss();
						}
					}
				});

			}
			try {

				galleryCursor = MediaStore.Images.Media.query(
						getContentResolver(),
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
						null, "date_added DESC");

				adapterGridPhotos = new AdapterGridPhotos(mLayoutInflater);

			} catch (Exception e) {
				isThereAnError = true;
			}

			return isThereAnError;
		}

		@Override
		protected void onPostExecute(Boolean isThereAnError) {
			// TODO Auto-generated method stub
			super.onPostExecute(isThereAnError);
			mProgressDialog.dismiss();
			if (isThereAnError) {
				AppMsg.makeText(HomeActivity.this,
						"Error loading gallery ,try again", AppMsg.STYLE_ALERT)
						.show();
			} else {

				gridPhotos.setAdapter(adapterGridPhotos);
				AppMsg.makeText(
						HomeActivity.this,
						"Gallery Loaded " + galleryCursor.getCount()
								+ " pictures", AppMsg.STYLE_INFO).show();
			}
		}

	}

	class AdapterGridPhotos extends BaseAdapter {

		class ViewHolder {
			ImageView imgPhoto;
			TextView labName;
		}

		LayoutInflater mInflater;

		public AdapterGridPhotos(LayoutInflater inflater) {
			mInflater = inflater;
		}

		@Override
		public int getCount() {
			return galleryCursor.getCount();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {

			galleryCursor.moveToPosition(position);
			return galleryCursor.getInt(0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {

			ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.thumbnail_view, null);
				holder = new ViewHolder();
				holder.imgPhoto = (ImageView) convertView
						.findViewById(R.id.img_thumbnail);
				holder.labName = (TextView) convertView
						.findViewById(R.id.lab_dysplay_name);
				convertView
						.setLayoutParams(new GridView.LayoutParams(100, 100));
				convertView.setTag(holder);
			}
			holder = (ViewHolder) convertView.getTag();
			galleryCursor.moveToPosition(position);
			int _id = galleryCursor.getInt(0);
			holder.imgPhoto.setImageBitmap(MediaStore.Images.Thumbnails
					.getThumbnail(getContentResolver(), _id,
							MediaStore.Images.Thumbnails.MICRO_KIND, null));

			// set dysplay name
			holder.labName.setText(galleryCursor.getString(3));

			return convertView;
		}

	}

	private final class AnActionModeOfEpicProportions implements
			ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Used to put dark icons on light action bar

			getSupportMenuInflater().inflate(R.menu.action_mode_grid, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			Toast.makeText(HomeActivity.this, "Got click: " + item,
					Toast.LENGTH_SHORT).show();
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		if (mMode == null) {
			Intent intentDetail = new Intent(HomeActivity.this,
					ImageDetailsActivity.class);
			intentDetail.putExtra(_ID_KEY,
					adapterGridPhotos.getItemId(position));
			startActivity(intentDetail);
		} else {
			if (view.isSelected()) {
				view.setSelected(false);
			} else {
				view.setSelected(true);
			}
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view,
			int position, long id) {
		if (mMode == null) {
			mMode = startActionMode(new AnActionModeOfEpicProportions());
		}
		mMode.setTitle("" + position);
		if (view.isSelected()) {
			view.setSelected(false);
		} else {
			view.setSelected(true);
		}
		return true;

	}

}
