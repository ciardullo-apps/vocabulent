package com.ciardullo.vocabulent.layout;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ciardullo.vocabulent.util.BitmapUtil;
import com.ciardullo.vocabulent.vo.Album;
import com.ciardullo.vocabulent.vo.Scene;
import com.ciardullo.vocabulent_it.R;

/**
 * Adapter to display all images in an Album
 */
public class ThumbnailAdapter extends BaseAdapter {

	private Context theContext;
	private static final int THUMB_WIDTH = 180;
	private static final int THUMB_HEIGHT = 120;
	
	/**
	 * The album to render in the GridView
	 */
	private Album album;
	
	public ThumbnailAdapter(Context theContext, Album album) {
		super();
		this.theContext = theContext;
		this.album = album;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	@Override
	public int getCount() {
		int count = 0;
		try {
			count = album.getAllScenes().size();
		} catch(Exception e) {}
		return count;
	}

	@Override
	public Object getItem(int position) {
		Scene scene = null;
		try {
			scene = album.getAllScenes().get(position);
		} catch(Exception e) {}
		return scene;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout miniScene = (LinearLayout) (convertView == null
	               ? LayoutInflater.from(theContext).inflate(R.layout.mini_scene, parent, false)
	               : convertView);
		ImageView iv = (ImageView)miniScene.findViewById(R.id.ivMiniScene);
		TextView tv = (TextView)miniScene.findViewById(R.id.tvMiniScene);
		ImageView ivKey = (ImageView)miniScene.findViewById(R.id.ivKey);
		
		if(album != null && album.getAllScenes() != null &&
				album.getAllScenes().size() > 0 && 
				position < album.getAllScenes().size()) {
			// There's an album with scenes
			Scene scene = album.getAllScenes().get(position);
			if(scene != null) {
				if(scene.isPurchased()) {
					iv.setBackgroundColor(Color.WHITE);
					ivKey.setVisibility(View.GONE);
				} else {
					iv.setBackgroundColor(Color.GRAY);
					ivKey.setVisibility(View.VISIBLE);
				}
				tv.setText(scene.getSceneDesc());
				BitmapWorkerTask task = 
						new BitmapWorkerTask(iv);
				task.execute(scene.getImageResourceId());
			}
		}
		iv.setLayoutParams(new RelativeLayout.LayoutParams(THUMB_WIDTH, THUMB_HEIGHT));
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        
		return miniScene;
	}

	/**
	 * Loads the images using a samplesize off of the UI thread
	 */
	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public BitmapWorkerTask(ImageView imageView) {
			super();
			this.imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(Integer... params) {
			Bitmap bitmap = null;
			if(params[0] != 0) {
				// Load the sampled bitmap
				bitmap = BitmapUtil.decodeSampledBitmapFromResource(
						theContext.getResources(), params[0], 
						THUMB_WIDTH, THUMB_HEIGHT);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if(imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if(imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}
}
/*
public class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;

    private Integer[] mImageIds = {
            R.drawable.sample_1,
            R.drawable.sample_2,
            R.drawable.sample_3,
            R.drawable.sample_4,
            R.drawable.sample_5,
            R.drawable.sample_6,
            R.drawable.sample_7
    };

    public ImageAdapter(Context c) {
        mContext = c;
       
        TypedArray a = c.obtainStyledAttributes(R.styleable.HelloGallery);
        mGalleryItemBackground = a.getResourceId(
                R.styleable.HelloGallery_android_galleryItemBackground, 0);
        a.recycle();
    }

    public int getCount() {
        return mImageIds.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(mContext);

        i.setImageResource(mImageIds[position]);
        i.setLayoutParams(new Gallery.LayoutParams(150, 100));
        i.setScaleType(ImageView.ScaleType.FIT_XY);
        i.setBackgroundResource(mGalleryItemBackground);

        return i;
    }
}
*/