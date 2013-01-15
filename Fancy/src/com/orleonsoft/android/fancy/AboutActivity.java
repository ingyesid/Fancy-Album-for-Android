package com.orleonsoft.android.fancy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.fima.cardsui.views.CardUI;
import com.orleonsoft.android.fancy.views.FancyAboutCard;

/**
 * File: AboutActivity.java Autor: Yesid Lazaro Mayoriano
 */

public class AboutActivity extends SherlockActivity {
	private CardUI mCardView;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		// init CardView
		mCardView = (CardUI) findViewById(R.id.cardsview);
		mCardView.setSwipeable(false);

		// add AndroidViews Cards

		mCardView.addCard(new FancyAboutCard("Fancy Album", "Version 1.0"));
		mCardView
				.addCardToLastStack(new FancyAboutCard(
						"Developed by",
						"Orleonsoft , al rigth reserved , more apps and contact in www.orleonsoft.com/contact ,"
								+ " contacto@orleonsoft.com , +573145377320"));
		mCardView.addCardToLastStack(new FancyAboutCard("Open Source Licences",
				"This app was developed thanks to ActionBarSherlock,CardUI"));

		// draw cards
		mCardView.refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_about, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Intent intent = new Intent(AboutActivity.this, HomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);

		}
		if (item.getItemId() == R.id.action_share_app) {
			launchShareTask(AboutActivity.this, getString(R.string.app_name), "Share");
		}

		return super.onOptionsItemSelected(item);
	}

	public static void launchShareTask(Context context, String title,
			String content) {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.putExtra(Intent.EXTRA_TEXT, content + " - via Fancy Album");
		context.startActivity(Intent.createChooser(intent, title));
	}

}
