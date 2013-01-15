package com.orleonsoft.android.fancy.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;
import com.orleonsoft.android.fancy.R;

public class FancyAboutCard extends Card {
	String mDescription;

	public FancyAboutCard(String title, String description) {

		super(title);
		mDescription = description;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context)
				.inflate(R.layout.card, null);
		((TextView) view.findViewById(R.id.title)).setText(title);
		((TextView) view.findViewById(R.id.description)).setText(mDescription);

		return view;
	}

}
