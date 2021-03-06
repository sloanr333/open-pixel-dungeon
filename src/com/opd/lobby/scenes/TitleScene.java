/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.opd.lobby.scenes;

import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;

import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.Touchscreen.Touch;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;
import com.opd.lobby.Assets;
import com.opd.lobby.Chrome;
import com.opd.lobby.effects.BannerSprites;
import com.opd.lobby.effects.Fireball;
import com.opd.lobby.scenes.PixelScene;
import com.opd.lobby.ui.Archs;
import com.opd.lobby.ui.ExitButton;
import com.opd.lobby.ui.GameInfoButton;
import com.opd.lobby.ui.ScrollPane;
import com.opd.opdlib.OPDGame;
import com.opd.opdlib.SubGame;
import com.opd.opdlib.JSONInterface;

public class TitleScene extends PixelScene {
	private static final String TXT_UPDATE = "Update Available!";
	private static final String LNK_UPDATE = "http://www.openpixeldungeon.com/download/";

	private static final int WIDTH = 120;

	private ScrollPane list;
	private ArrayList<ListItem> subGamesList = new ArrayList<TitleScene.ListItem>();

	@Override
	public void create() {
		super.create();

		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;

		float height = 180;

		Archs archs = new Archs();
		archs.setSize(w, h);
		add(archs);

		Image title = BannerSprites.get(BannerSprites.Type.PIXEL_DUNGEON);
		add(title);

		title.x = (w - title.width()) / 2;
		title.y = (h - height) / 2;

		placeTorch(title.x + 18, title.y + 20);
		placeTorch(title.x + title.width - 18, title.y + 20);

		int yPos = (int) (title.y + title.height());

		list = new ScrollPane(new Component()) {
			@Override
			public void onClick(float x, float y) {
				int size = subGamesList.size();
				for (int i = 0; i < size; i++) {
					if (subGamesList.get(i).onClick(x, y)) {
						break;
					}
				}
			}
		};
		add(list);
		list.setRect((w - WIDTH) / 2, yPos, WIDTH, h - yPos);

		updateList();

		ExitButton btnExit = new ExitButton();
		btnExit.setPos(w - btnExit.width(), 0);
		add(btnExit);

		if (JSONInterface.updateAvailable()) {
			BitmapTextMultiline link = createMultiline( TXT_UPDATE, 8 );
			link.maxWidth = Math.min( Camera.main.width, 120 );
			link.measure();
			link.hardlight( Window.TITLE_COLOR );
			add( link );
			
			link.x = 0;
			link.y = h-link.height();
			
			TouchArea hotArea = new TouchArea( link ) {
				@Override
				protected void onClick( Touch touch ) {
					Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( LNK_UPDATE ) );
					Game.instance.startActivity( intent );
				}
			};
			add( hotArea );
		}
		
		displayVersion(w, h);

		fadeIn();
	}

	private void placeTorch(float x, float y) {
		Fireball fb = new Fireball();
		fb.setPos(x, y);
		add(fb);
	}

	private void updateList() {
		subGamesList.clear();

		Component content = list.content();
		content.clear();
		list.scrollTo(0, 0);

		float pos = 0;
		for (SubGame theSubGame : OPDGame.subGames()) {
			ListItem gameBtn = new ListItem(theSubGame);
			gameBtn.setRect(0, pos, WIDTH, gameBtn.height());
			content.add(gameBtn);
			subGamesList.add(gameBtn);

			pos += gameBtn.height();
		}

		content.setSize(WIDTH, pos);
	}

	private static class ListItem extends Button {
		private SubGame subGame;
		private Image image;
		private NinePatch bg;
		private BitmapText gameName;
		private BitmapText gameAuthor;
		private BitmapText gameVersion;
		private GameInfoButton btnGameInfo;

		private static final int GAP = 2;
		private static final int MARGIN = 7;
		private static final int BUTTON_WIDTH = WIDTH;
		private static final int BUTTON_HEIGHT = 38;

		@Override
		protected void createChildren() {
			super.createChildren();
			/*
			 * This happens first, presumably it's called by a super
			 * constructor.
			 */

			bg = Chrome.get(Chrome.Type.WINDOW);
			add(bg);
			image = new Image();
			add(image);
			gameName = createText(9);
			add(gameName);
			gameAuthor = createText(6);
			add(gameAuthor);
			gameVersion = createText(6);
			add(gameVersion);

			btnGameInfo = new GameInfoButton();
			add(btnGameInfo);
		}

		public ListItem(final SubGame banans) {
			super();
			/* This happens second */
			this.subGame = banans;

			image.texture(subGame.asset);

			gameName.text(subGame.title);
			gameName.measure();

			gameAuthor.text("Author: " + subGame.author);
			gameAuthor.measure();
			gameAuthor.hardlight(0xaaaaaa);

			gameVersion.text("v" + subGame.version);
			gameVersion.measure();
			gameVersion.hardlight(0x888888);

			setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
		}

		@Override
		protected void layout() {
			/* This happens third, and or fourth, and or maybe fifth. idk. */
			bg.size(BUTTON_WIDTH, BUTTON_HEIGHT);

			bg.x = x;
			bg.y = y;

			gameName.x = align(x + MARGIN);
			gameName.y = align(y + MARGIN);

			image.x = align(gameName.x);
			image.y = align(gameName.y + gameName.baseLine() + GAP - 1);

			gameAuthor.x = align(image.x + image.width + GAP);
			gameAuthor.y = align(gameName.y + gameName.baseLine() + GAP);

			gameVersion.x = align(image.x + image.width + GAP);
			gameVersion.y = align(gameAuthor.y + gameAuthor.baseLine() + GAP);

			btnGameInfo.setSubGame(subGame);
			btnGameInfo.setPos(BUTTON_WIDTH - btnGameInfo.width() - GAP * 2,
					bg.y + BUTTON_HEIGHT - btnGameInfo.height() - GAP * 2);
		}

		public boolean onClick(float x, float y) {
			if (inside(x, y)) {
				Sample.INSTANCE.play(Assets.SND_DESCEND, 1, 1, 1.2f);
				OPDGame.switchGame(subGame);
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void onTouchDown() {
			image.brightness(1.5f);
			Sample.INSTANCE.play(Assets.SND_CLICK, 1, 1, 0.8f);
		}

		@Override
		protected void onTouchUp() {
			image.resetColor();
		}
	}
}
