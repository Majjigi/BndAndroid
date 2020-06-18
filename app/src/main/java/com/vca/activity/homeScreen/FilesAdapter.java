package com.vca.activity.homeScreen;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.squareup.picasso.Picasso;
import com.vca.R;
import com.vca.utils.dropbox.FileThumbnailRequestHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;

/**
 * Adapter for file list
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.MetadataViewHolder> {
    private List<Metadata> mFiles;
    private final Picasso mPicasso;
    private final Callback mCallback;
    private Context mContext;

    public void setFiles(List<Metadata> files) {
        mFiles = Collections.unmodifiableList(new ArrayList<>(files));
        notifyDataSetChanged();
    }

    public interface Callback {
        void onFolderClicked(FolderMetadata folder);

        void onFileClicked(FileMetadata file);

        void onDeleteFileClicked(String filePath);
    }

    public FilesAdapter(Context context, Picasso picasso, Callback callback) {
        mContext = context;
        mPicasso = picasso;
        mCallback = callback;
    }

    @Override
    public MetadataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.files_item, viewGroup, false);
        return new MetadataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MetadataViewHolder metadataViewHolder, int i) {
        metadataViewHolder.bind(metadataViewHolder, mFiles.get(i));
    }

    @Override
    public long getItemId(int position) {
        return mFiles.get(position).getPathLower().hashCode();
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    public class MetadataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;
        private final ImageView mImageView;
        private Metadata mItem;
        public ImageButton menuButton;

        public MetadataViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image);
            mTextView = (TextView) itemView.findViewById(R.id.text);
            menuButton = (ImageButton) itemView.findViewById(R.id.menu);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItem instanceof FolderMetadata) {
                mCallback.onFolderClicked((FolderMetadata) mItem);
            } else if (mItem instanceof FileMetadata) {
                mCallback.onFileClicked((FileMetadata) mItem);
            }
        }

        public void bind(final MetadataViewHolder metadataViewHolder, Metadata item) {
            mItem = item;
            mTextView.setText(mItem.getName());

            metadataViewHolder.menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(mContext, metadataViewHolder.menuButton, Gravity.END);
                    popup.getMenuInflater().inflate(R.menu.menu_device_details, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int itemId = item.getItemId();
                            switch (itemId) {
                                case R.id.action_delete:
                                    mCallback.onDeleteFileClicked(mItem.getPathLower());
                                    break;
                            }
                            return true;
                        }
                    });
                    popup.show();
                }
            });
            // Load based on file path
            // Prepending a magic scheme to get it to
            // be picked up by DropboxPicassoRequestHandler

            if (item instanceof FileMetadata) {
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = item.getName().substring(item.getName().indexOf(".") + 1);
                String type = mime.getMimeTypeFromExtension(ext);
                if (type != null && type.startsWith("image/")) {
                    mPicasso.load(FileThumbnailRequestHandler.buildPicassoUri((FileMetadata) item))
                            .placeholder(R.drawable.ic_photo_grey_600_36dp)
                            .error(R.drawable.ic_photo_grey_600_36dp)
                            .into(mImageView);
                } else {
                    mPicasso.load(R.drawable.ic_insert_drive_file_blue_36dp)
                            .noFade()
                            .into(mImageView);
                }
            } else if (item instanceof FolderMetadata) {
                mPicasso.load(R.drawable.ic_folder_blue_36dp)
                        .noFade()
                        .into(mImageView);
            }
        }
    }
}
