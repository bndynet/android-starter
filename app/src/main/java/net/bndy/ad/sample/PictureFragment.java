package net.bndy.ad.sample;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.bndy.ad.R;
import net.bndy.ad.framework.BaseActivity;
import net.bndy.ad.framework.BaseFragment;
import net.bndy.ad.framework.CallbackHandler1;
import net.bndy.ad.framework.CallbackHandler2;
import net.bndy.ad.framework.system.CameraHelper;

public class PictureFragment extends BaseFragment {

    private ImageView mImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        mImageView = layout.findViewById(R.id.sample_picture_tv);
        layout.findViewById(R.id.sample_picture_take_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)getActivity()).startTakePhoto(new CallbackHandler1<Bitmap>() {
                    @Override
                    public void callback(Bitmap bitmap) {
                        mImageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
        layout.findViewById(R.id.sample_picture_choose_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startChoosePhoto(new CallbackHandler2<Uri, Bitmap>() {
                    @Override
                    public void callback(Uri arg1, Bitmap arg2) {
                        mImageView.setImageBitmap(arg2);
                    }
                });
            }
        });

        return layout;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_picture;
    }
}
