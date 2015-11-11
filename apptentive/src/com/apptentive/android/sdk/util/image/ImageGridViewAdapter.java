package com.apptentive.android.sdk.util.image;

/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apptentive.android.sdk.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.view.ApptentiveMaterialDeterminateProgressBar;


public class ImageGridViewAdapter extends BaseAdapter {

	private static final int TYPE_CAMERA = 0;
	private static final int TYPE_IMAGE = 1;
	public static final int GONE = 0x00000008;

	private LayoutInflater inflater;
	private boolean showCamera = true;
	private boolean showImageIndicator = true;
	private int defaultImageIndicator;
	private Callback localCallback;

	private List<ImageItem> images = new ArrayList<ImageItem>();
	private List<ImageItem> selectedImages = new ArrayList<ImageItem>();

	private List<ImageItem> downloadItems = new ArrayList<ImageItem>();

	private int itemSize;
	private GridView.LayoutParams itemLayoutParams;

	public ImageGridViewAdapter(Context context, boolean showCamera) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.showCamera = showCamera;
		itemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
	}

	/**
	 * selection indicator
	 *
	 * @param bval if true, overlay an indicator on the upper-right corner
	 */
	public void showImageIndicator(boolean bval) {
		showImageIndicator = bval;
	}

	/**
	 * selection indicator
	 *
	 * @param rid if true, overlay an indicator on the upper-right corner
	 */
	public void setImageIndicator(int rid) {
		defaultImageIndicator = rid;
	}

	public void setShowCamera(boolean bval) {
		if (showCamera == bval) {
			return;
		}

		showCamera = bval;
		notifyDataSetChanged();
	}

	public boolean isShowCamera() {
		return showCamera;
	}

	public void setIndicatorCallback(Callback localCallback) {
		this.localCallback = localCallback;
	}

	/**
	 * Click an image
	 *
	 * @param index
	 */
	public boolean cilickOn(int index) {
		ImageItem item = getItem(index);
		String fileType = item.mimeType.substring(0, item.mimeType.indexOf("/"));
		if (!fileType.equalsIgnoreCase("Image")) {
			if (downloadItems.contains(item)) {
				return true;
			} else {
				File localFile = new File(item.localCachePath);
				if (localFile.exists()) {
					return false;
				} else {
					downloadItems.add(item);
					notifyDataSetChanged();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Select an image
	 *
	 * @param image
	 */
	public void select(ImageItem image) {
		if (selectedImages.contains(image)) {
			selectedImages.remove(image);
		} else {
			selectedImages.add(image);
		}
		notifyDataSetChanged();
	}

	/**
	 * Re-Select image from selected list result
	 *
	 * @param resultList
	 */
	public void setDefaultSelected(ArrayList<String> resultList) {
		for (String uri : resultList) {
			ImageItem image = getImageByUri(uri);
			if (image != null) {
				selectedImages.add(image);
			}
		}
		if (selectedImages.size() > 0) {
			notifyDataSetChanged();
		}
	}

	private ImageItem getImageByUri(String uri) {
		if (images != null && images.size() > 0) {
			for (ImageItem image : images) {
				if (image.originalPath.equalsIgnoreCase(uri)) {
					return image;
				}
			}
		}
		return null;
	}

	/**
	 * Re-select image from selected image set
	 *
	 * @param images
	 */
	public void setData(List<ImageItem> images) {
		selectedImages.clear();

		if (images != null && images.size() > 0) {
			this.images = images;
		} else {
			this.images.clear();
		}
		notifyDataSetChanged();
	}

	/**
	 * Reset colum size
	 *
	 * @param columnWidth
	 */
	public void setItemSize(int columnWidth) {

		if (itemSize == columnWidth) {
			return;
		}

		itemSize = columnWidth;

		itemLayoutParams = new GridView.LayoutParams(itemSize, itemSize);

		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (showCamera) {
			return position == 0 ? TYPE_CAMERA : TYPE_IMAGE;
		}
		return TYPE_IMAGE;
	}

	@Override
	public int getCount() {
		return showCamera ? images.size() + 1 : images.size();
	}

	@Override
	public ImageItem getItem(int i) {
		if (showCamera) {
			if (i == 0) {
				return null;
			}
			return images.get(i - 1);
		} else {
			return images.get(i);
		}
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {

		int type = getItemViewType(i);
		if (type == TYPE_CAMERA) {
			//view = inflater.inflate(R.layout.apptentive_image_picker_item_camera, viewGroup, false);
			view.setTag(null);
		} else if (type == TYPE_IMAGE) {
			ViewHolder holder;
			if (view == null) {
				view = inflater.inflate(R.layout.apptentive_image_grid_view_item, viewGroup, false);
				holder = new ViewHolder(view);
			} else {
				holder = (ViewHolder) view.getTag();
				if (holder == null) {
					view = inflater.inflate(R.layout.apptentive_image_grid_view_item, viewGroup, false);
					holder = new ViewHolder(view);
				}
			}
			if (holder != null) {
				holder.bindData(i);
			}
		}

		/** Fixed View Size */
		GridView.LayoutParams lp = (GridView.LayoutParams) view.getLayoutParams();
		if (lp.height != itemSize) {
			view.setLayoutParams(itemLayoutParams);
		}

		return view;
	}

	class ViewHolder {
		ImageView image;
		ImageView indicator;
		TextView preview_title;
		ProgressBar progressBar;
		ApptentiveMaterialDeterminateProgressBar progressBar_horizontal;
		View mask;
		boolean bLoadThumbnail;

		ViewHolder(View view) {
			image = (ImageView) view.findViewById(R.id.image);
			indicator = (ImageView) view.findViewById(R.id.indicator);
			mask = view.findViewById(R.id.mask);
			preview_title = (TextView) view.findViewById(R.id.image_preview_title);
			progressBar = (ProgressBar) view.findViewById(R.id.thumbnail_progress);
			progressBar_horizontal = (ApptentiveMaterialDeterminateProgressBar) view.findViewById(R.id.thumbnail_progress_determinate);
			view.setTag(this);
		}

		void bindData(final int index) {
			final ImageItem data = getItem(index);
			if (data == null) {
				return;
			}

			int placeholderResId = R.drawable.apptentive_ic_image_default_item;

			if (showImageIndicator) {
				indicator.setVisibility(View.VISIBLE);
				image.setVisibility(View.VISIBLE);
				if (selectedImages.contains(data)) {
					// set as selected
					indicator.setImageResource(R.drawable.apptentive_ic_image_picker_selected);
					mask.setVisibility(View.VISIBLE);
				} else {
					// set default indicator
					if (data.originalPath == null) {
						indicator.setVisibility(View.GONE);
						image.setVisibility(View.GONE);
					} else {
						indicator.setImageResource(defaultImageIndicator);
						indicator.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								if (localCallback != null) {
									localCallback.onImageSelected(index);
								}
							}
						});
					}
					mask.setVisibility(View.GONE);
				}
			} else {
				indicator.setVisibility(View.GONE);
				if (data.originalPath == null) {
					image.setVisibility(View.GONE);
				} else {
					image.setVisibility(View.VISIBLE);
				}
			}

			String fileType = data.mimeType.substring(0, data.mimeType.indexOf("/"));
			if (fileType.equalsIgnoreCase("Image")) {
				preview_title.setVisibility(View.GONE);
				bLoadThumbnail = true;
				// show the progress bar while we load content...
				progressBar.setVisibility(View.VISIBLE);
			} else {
				bLoadThumbnail = false;
				progressBar.setVisibility(View.GONE);
				preview_title.setVisibility(View.VISIBLE);
				preview_title.setText(MimeTypeMap.getSingleton().getExtensionFromMimeType(data.mimeType));
				placeholderResId = R.drawable.apptentive_generic_file_thumbnail;
				if (downloadItems.contains(data)) {
					if (progressBar_horizontal != null) {
						progressBar_horizontal.setVisibility(View.VISIBLE);
					}
				} else {
					if (progressBar_horizontal != null) {
						progressBar_horizontal.setVisibility(View.GONE);
					}
				}
			}

			image.setImageDrawable(null);
			image.setImageResource(placeholderResId);

			if (itemSize > 0 && data.originalPath != null) {
				if (bLoadThumbnail) {
					ApptentiveAttachmentLoader.getInstance().load(data.originalPath, data.localCachePath, index, image, itemSize, itemSize, true,
							new ApptentiveAttachmentLoader.LoaderCallback() {
								@Override
								public void onLoaded(ImageView view, int pos, Drawable d) {
									if (progressBar != null) {
										progressBar.setVisibility(View.GONE);
									}
									if (index == pos && image == view && bLoadThumbnail) {
										image.setImageDrawable(d);
									}
								}

								@Override
								public void onDownloadStart() {
									if (progressBar != null) {
										progressBar.setVisibility(View.GONE);
									}
									if (progressBar_horizontal != null) {
										progressBar_horizontal.setVisibility(View.VISIBLE);
									}
								}

								@Override
								public void onDownloadProgress(int progress) {
									if (progressBar_horizontal != null) {
										if (progress == 100 || progress == -1) {
											progressBar_horizontal.setVisibility(View.GONE);
											if (progressBar != null) {
												progressBar.setVisibility(View.VISIBLE);
											}
										}
										if (progress >= 0) {
											progressBar_horizontal.setProgress(progress);
										}
									}
								}
							});
				} else if (downloadItems.contains(data)){
					ApptentiveAttachmentLoader.getInstance().load(data.originalPath, data.localCachePath, index, image, 0, 0, false,
							new ApptentiveAttachmentLoader.LoaderCallback() {
								@Override
								public void onLoaded(ImageView view, int pos, Drawable d) {
									downloadItems.remove(data);
									Util.openFileAttachment(view.getContext(), data.localCachePath, data.mimeType);
								}

								@Override
								public void onDownloadStart() {
									if (progressBar_horizontal != null) {
										progressBar_horizontal.setVisibility(View.VISIBLE);
									}
								}

								@Override
								public void onDownloadProgress(int progress) {
									if (progressBar_horizontal != null) {
										if (progress == 100 || progress == -1) {
											progressBar_horizontal.setVisibility(View.GONE);
										}
										if (progress >= 0) {
											progressBar_horizontal.setProgress(progress);
										}
									}
								}
							});
				}
			}
		}
	}

	/**
	 * Callback Interface
	 */
	public interface Callback {
		void onSingleImageSelected(int index);

		void onImageSelected(int index);

		void onImageUnselected(String path);

		void onCameraShot(File imageFile);
	}

}