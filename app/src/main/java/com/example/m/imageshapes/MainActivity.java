package com.example.m.imageshapes;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;

import net.karthikraj.shapesimage.ShapesImage;
import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    private ShapesImage img1,img2,img3,img4;
    private ImageView pick,img1_btn,img2_btn,img3_btn,img4_btn,fm;
    private Bitmap bitmap1,bitmap2,bitmap3,bitmap4;
    private final int img0_req=0;
    private final int img1_req=1;
    private final int img2_req=2;
    private final int img3_req=3;
    private final int img4_req=4;
    private final int max_img_num=4;
    private List<Bitmap> bitmapList;

    private List<ShapesImage> circularImageViewList;
    private LinearLayout root;
    private Button save;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

    ColorPickerDialog pickcolor;
    int color;

    RelativeLayout relative;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
       CreateClipIntent();
    }

    private void CreateClipIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(intent.createChooser(intent,"choose image"),img0_req);
    }

    private void initView() {
        bitmapList = new ArrayList<>();
        circularImageViewList = new ArrayList<>();
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img4 = findViewById(R.id.img4);
        fm = findViewById(R.id.fm);
        root = findViewById(R.id.root);
        img1_btn = findViewById(R.id.img1_btn);
        img2_btn = findViewById(R.id.img2_btn);
        img3_btn = findViewById(R.id.img3_btn);
        img4_btn = findViewById(R.id.img4_btn);
        pick = findViewById(R.id.pick);
        relative=findViewById(R.id.relative);


        circularImageViewList.add(img1);
        circularImageViewList.add(img2);
        circularImageViewList.add(img3);
        circularImageViewList.add(img4);

        save  = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fm.setImageBitmap(Save(root));
            }
        });
        img1.setOnTouchListener(this);
        img2.setOnTouchListener(this);
        img3.setOnTouchListener(this);
        img4.setOnTouchListener(this);

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                color= Color.parseColor("#ffffff");
                pickcolor=new ColorPickerDialog(MainActivity.this,color);
                pickcolor.setAlphaSliderVisible(true);
                pickcolor.setTitle("PICK");

                pickcolor.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int color) {



                        relative.setBackgroundColor(color);




                    }
                });


                pickcolor.show();


            }
        });


    }

    private Bitmap Save(View root) {
        root.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(root.getDrawingCache());
        root.setDrawingCacheEnabled(false);
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==img0_req && resultCode==RESULT_OK&&data!=null)
        {
            ClipData clipData = data.getClipData();
            if (clipData!=null)
            {
                for (int i =0;i<clipData.getItemCount();i++)
                {
                    ClipData.Item item = clipData.getItemAt(i);
                    Uri uri = item.getUri();
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        bitmapList.add(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }

                if (bitmapList.size()<=4)
                {
                    UpdateUi(bitmapList);

                }else
                    {
                        Toast.makeText(this, "Choose 4 img", Toast.LENGTH_SHORT).show();
                        CreateClipIntent();

                    }

            }else
                {
                    CreateClipIntent();
                    Toast.makeText(this, "Choose 4 img", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void UpdateUi(List<Bitmap> bitmapList) {
        for (int i=0;i<bitmapList.size();i++)
        {
            circularImageViewList.get(i).getImageMatrix().postScale(0.2f,0.2f);
            circularImageViewList.get(i).setImageBitmap(bitmapList.get(i));
        }

        bitmapList.clear();
        img1_btn.setVisibility(View.GONE);
        img2_btn.setVisibility(View.GONE);
        img3_btn.setVisibility(View.GONE);
        img4_btn.setVisibility(View.GONE);

    }

    public boolean onTouch(View v, MotionEvent event) {
        // handle touch events here
        ShapesImage view = (ShapesImage) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 2) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view.getWidth() / 2) * sx;
                        float yc = (view.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        return true;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return ( float) Math.sqrt(x * x + y * y);
    }



    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }
}
