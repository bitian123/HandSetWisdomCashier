package com.centerm.epos.print.receipt;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;


import com.centerm.cloudsys.sdk.common.utils.FileUtils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * 电子签购单生成器。
 * <p>
 * author:wanliang527</br>
 * date:2017/3/6</br>
 */
public class EleReceiptCreator {

    private final static int VERTICAL_PADDING = 20;//上下边距
    private final static int HORIZONTAL_PADDING = 0;//左右边距。一般设定为0，因为打印机本身在打印时，会自动在左右两边留白。
    private final static int VALID_WIDTH = 17 * FontSize.MEDIUM.getValue();
    private final static int TOTAL_WIDTH = VALID_WIDTH + 2 * HORIZONTAL_PADDING;//中号字体大概可以容纳17个字，这个是固定的宽度，高度自适应
    private int picMaxHeight = TOTAL_WIDTH / 3 * 2;//最大高度不超过宽的2/3

    private Logger logger = Logger.getLogger(getClass());
    private TextPaint textPaint;
    private List<IReceiptElement> eleList = new ArrayList<>();

    private String saveDir;//生成的电子签购单需要保存路径
    private String batchNo;//批次号
    private String serialNo;//流水号

    private boolean includepad = false;//指明绘制文本信息时，是否需要在顶部和底部保留间距

    private EleReceiptCreator() {
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
    }

    /**
     * 创建实例对象。
     *
     * @param saveDir  签购单保存路径
     * @param batchNo  批次号
     * @param serialNo 流水号
     * @return 生成器实例
     */
    public static EleReceiptCreator newInstance(String saveDir, String batchNo, String serialNo) {
        EleReceiptCreator creator = new EleReceiptCreator();
        creator.saveDir = saveDir;
        creator.batchNo = batchNo;
        creator.serialNo = serialNo;
        if (TextUtils.isEmpty(saveDir) || TextUtils.isEmpty(batchNo) || TextUtils.isEmpty(serialNo)) {
            throw new InvalidParameterException("The params cannot be empty");
        } else {
            File file = new File(saveDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return creator;
    }

    /**
     * 添加一个签购单元素。参考{@link TextElement},{@link PicElement},{@link TextGroupElement}
     *
     * @param ele 签购单元素
     * @return 本对象
     */
    public EleReceiptCreator add(IReceiptElement ele) {
        eleList.add(ele);
        return this;
    }

    /**
     * 添加一组签购单元素。
     *
     * @param list 签购单元素列表
     * @return 本对象
     */
    public EleReceiptCreator addAll(List<IReceiptElement> list) {
        if (list == null) {
            return this;
        }
        eleList.addAll(list);
        return this;
    }

    /**
     * 开始创建电子签购单。注意：该方法是同步且耗时操作，必要时，调用者需要进行异步处理。
     *
     * @param context Context
     * @return 图片文件对象
     */
    public File create(Context context) {
        int[] size = onMeasure(context);
        int width = size[0];
        int height = size[1];
        //记录画布原点位置
        int posX = HORIZONTAL_PADDING;
        int posY = VERTICAL_PADDING;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        for (int i = 0; i < eleList.size(); i++) {
            IReceiptElement ele = eleList.get(i);
            if (ele instanceof TextElement) {
                TextElement item = (TextElement) ele;
                canvas.save();
                canvas.translate(posX, posY);
                StaticLayout layout = getStaticLayout(item, VALID_WIDTH);
                layout.draw(canvas);
                int h = layout.getHeight();
                posY += h;
                canvas.restore();
            } else if (ele instanceof TextGroupElement) {
                TextGroupElement group = (TextGroupElement) ele;
                TextElement left = group.getLeftItem();
                TextElement right = group.getRightItem();
                canvas.save();
                canvas.translate(posX, posY);
                if (group.isSingleLine()) {
                    StaticLayout leftLayout = getStaticLayout(left, VALID_WIDTH);
                    leftLayout.draw(canvas);
                    canvas.translate(leftLayout.getWidth() + group.getSpacing(), 0);
                    StaticLayout rightLayout = getStaticLayout(right, VALID_WIDTH);
                    rightLayout.draw(canvas);
                    if (leftLayout.getHeight() > rightLayout.getHeight()) {
                        posY += leftLayout.getHeight();
                    } else {
                        posY += rightLayout.getHeight();
                    }
                } else {
                    StaticLayout leftLayout = getStaticLayout(left, (VALID_WIDTH - group.getSpacing()) / 2);
                    leftLayout.draw(canvas);
                    canvas.translate((VALID_WIDTH + group.getSpacing()) / 2, 0);
                    StaticLayout rightLayout = getStaticLayout(right, (VALID_WIDTH - group.getSpacing()) / 2);
                    rightLayout.draw(canvas);
                    if (leftLayout.getHeight() > rightLayout.getHeight()) {
                        posY += leftLayout.getHeight();
                    } else {
                        posY += rightLayout.getHeight();
                    }
                }
                canvas.restore();
            } else if (ele instanceof PicElement) {
                PicElement item = (PicElement) ele;
                Bitmap b = getBitmap(context, item);
                if (b != null) {
                    Rect src = new Rect(0, 0, b.getWidth(), b.getHeight());
                    int[] s = adjustBitmapSize(b, item.getWidth());
                    int left = HORIZONTAL_PADDING;
                    switch (item.getAlign()) {
                        case LEFT:
                            break;
                        case CENTER:
                            left = (width - s[0]) / 2;
                            break;
                        case RIGHT:
                            left = width - HORIZONTAL_PADDING - s[0];
                            break;
                    }
                    Rect dst = new Rect(left, posY, left + s[0], posY + s[1]);
                    if (!b.isRecycled()) {
                        canvas.drawBitmap(b, src, dst, null);
                        b.recycle();
                        posY += s[1];
                    }
                }
            }
        }
        if (saveToFile(bitmap)) {
            return new File(getFileName());
        }
        return null;
    }


    /**
     * 开始创建电子签购单。异步方式。
     *
     * @param context  Context
     * @param callback 回调对象
     */
    public void asynCreate(Context context, final Callback callback) {
        new AsyncTask<Context, Integer, File>() {
            @Override
            protected File doInBackground(Context... params) {
                if (params == null || params.length < 1) {
                    return null;
                }
                return create(params[0]);
            }

            @Override
            protected void onPostExecute(File file) {
                if (callback != null) {
                    callback.onComplete(file);
                }
            }
        }.execute(context);
    }

    private StaticLayout getStaticLayout(TextElement element, int widthLimited) {
        Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
        switch (element.getAlign()) {
            case LEFT:
                alignment = Layout.Alignment.ALIGN_NORMAL;
                break;
            case CENTER:
                alignment = Layout.Alignment.ALIGN_CENTER;
                break;
            case RIGHT:
                alignment = Layout.Alignment.ALIGN_OPPOSITE;
                break;
        }
        setTextPaint(element);
        return new StaticLayout(element.getContent(), textPaint, widthLimited, alignment, 1.0f, 0.0f, includepad);
    }

    private Bitmap getBitmap(Context context, PicElement element) {
        Bitmap b = TextUtils.isEmpty(element.getFilePath()) ? null : decodeFromFile(element.getFilePath());
        if (b == null) {
            b = element.getResId() > 0 ? decodeFromDrawable(context, element.getResId()) : null;
        }
        if (b == null) {
            b = element.getBmp();
        }
        return b;
    }

    private void setTextPaint(TextElement item) {
        textPaint.setTextSize(item.getSize().getValue());
        textPaint.setColor(item.getGrey().getColor());
        if (item.isBold()) {
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            textPaint.setTypeface(Typeface.DEFAULT);
        }
        textPaint.setUnderlineText(item.isUnderline());
    }

    private int[] adjustBitmapSize(Bitmap bitmap, int defWidth) {
        int[] size = new int[2];
        if (bitmap != null) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            if (defWidth > 0) {
                //如果外部有指定宽度的话
                size[0] = defWidth;
            } else {
                size[0] = w;
            }
            if (size[0] > TOTAL_WIDTH) {
                //最大宽度不超过凭条宽度
                size[0] = TOTAL_WIDTH;
            }
            //计算高度，最大不超过凭条宽度的三分之二
            if (size[0] / w > 1) {
                //被放大了，高度也对应放大
                size[1] = (int) (h * (size[0] / (double) w));
            } else {
                //被缩小了，高度也对应缩小
                size[1] = (int) (h / (w / (double) size[0]));
            }
            if (size[1] > picMaxHeight) {
                size[1] = picMaxHeight;
            }
        }
        return size;
    }

    private int[] onMeasure(Context context) {
        int[] size = new int[]{TOTAL_WIDTH, VERTICAL_PADDING * 2};
        for (int i = 0; i < eleList.size(); i++) {
            IReceiptElement item = eleList.get(i);
            if (item instanceof TextElement) {
                StaticLayout layout = getStaticLayout((TextElement) item, VALID_WIDTH);
                layout.draw(new Canvas());
                size[1] += layout.getHeight();
            } else if (item instanceof TextGroupElement) {
                TextGroupElement group = (TextGroupElement) item;
                TextElement left = group.getLeftItem();
                TextElement right = group.getRightItem();
                Canvas canvas = new Canvas();
                StaticLayout leftLayout = getStaticLayout(left, VALID_WIDTH);
                leftLayout.draw(canvas);
                StaticLayout rightLayout = getStaticLayout(right, VALID_WIDTH);
                rightLayout.draw(canvas);
                if (leftLayout.getWidth() + rightLayout.getWidth() + group.getSpacing() > VALID_WIDTH) {
                    //总宽度大于行宽，需要分两行显示
                    group.setSingleLine(false);
//                    left.setAlign(Paint.Align.LEFT);
                    leftLayout = getStaticLayout(left, (VALID_WIDTH - group.getSpacing()) / 2);
                    leftLayout.draw(canvas);
//                    right.setAlign(Paint.Align.LEFT);
                    rightLayout = getStaticLayout(right, (VALID_WIDTH - group.getSpacing()) / 2);
                    rightLayout.draw(canvas);
                }
                if (leftLayout.getHeight() > rightLayout.getHeight()) {
                    size[1] += leftLayout.getHeight();
                } else {
                    size[1] += rightLayout.getHeight();
                }
            } else if (item instanceof PicElement) {
                PicElement picEle = (PicElement) item;
                Bitmap b = getBitmap(context, picEle);
                if (b != null) {
                    size[1] += b.getHeight();
                }
            }
        }
        return size;
    }

  /*  private int[] measureText(TextElement ele) {
        int[] size = new int[2];
        FontSize font = ele.getSize();
        measurePaint.setTextSize(font.getValue());
        size[0] = (int) measurePaint.measureText(ele.getContent());
        Paint.FontMetrics fm = measurePaint.getFontMetrics();
        size[1] = (int) (Math.abs(fm.ascent) + Math.abs(fm.descent));
        logger.info(ele.toString());
        logger.warn("宽度：" + size[0] + "==>高度：" + size[1]);
        return size;
    }

    private int[] measurePic(PicElement ele) {
        int[] size = new int[2];
        return size;
    }*/

    private Bitmap decodeFromFile(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    private Bitmap decodeFromDrawable(Context context, int resId) {
        return BitmapFactory.decodeResource(context.getResources(), resId);
      /*  try {
            BitmapDrawable code = (BitmapDrawable) context.getResources().getDrawable(resId);
            Bitmap codeBitmap = code.getBitmap();
            return codeBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;*/
    }

    private boolean saveToFile(Bitmap bitmap) {
        File file = new File(getFileName());
        if (file.exists()) {
            logger.warn(file.getAbsoluteFile() + "==>文件已存在==>删除");
            file.delete();
        }
        try {
            FileOutputStream fps = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fps);
            fps.flush();
            fps.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getFileName() {
        File file = new File(saveDir);
        if (!file.exists()) {
            FileUtils.createDirectory(saveDir);
        }
        return saveDir + File.separator + batchNo + "_" + serialNo + ".png";
    }

    public void demo(Context context) {
        TextElement ele1 = new TextElement("中号字体123ABCabc");
        TextElement ele2 = new TextElement("小号字体123ABCabc");
        ele2.setSize(FontSize.SMALL);

        TextElement ele3 = new TextElement("大号字体123ABCabc");
        ele3.setSize(FontSize.LARGE);

        TextElement ele4 = new TextElement("居中居中居中123ABCabc");
        ele4.setAlign(Paint.Align.CENTER);

        TextElement ele5 = new TextElement("靠右靠右靠右靠右123ABCabc");
        ele5.setAlign(Paint.Align.RIGHT);

        TextElement ele6 = new TextElement("居中换行居中换行居中换行居中换行居中换行123ABCabc");
        ele6.setAlign(Paint.Align.CENTER);

        TextElement ele7 = new TextElement("靠右换行靠右换行靠右换行靠右换行靠右换行123ABCabc靠右换行靠右换行靠右换行靠右换行靠右换行靠右换行");
        ele7.setAlign(Paint.Align.RIGHT);

        TextElement ele8 = new TextElement("小号加粗字体123ABCabc");
        ele8.setSize(FontSize.SMALL);
        ele8.setBold(true);

        TextElement ele9 = new TextElement("中号加粗字体123ABCabc");
        ele9.setBold(true);

        TextElement ele10 = new TextElement("中号带下划线字体123ABCabc");
        ele10.setUnderline(true);

//        PicElement ele11 = new PicElement(R.drawable.ic_launcher);
//        PicElement ele12 = new PicElement(R.drawable.ic_launcher);
//        ele12.setAlign(Paint.Align.LEFT);
//        PicElement ele13 = new PicElement(R.drawable.ic_launcher);
//        ele13.setAlign(Paint.Align.RIGHT);

//        TextElement ele14 = new TextElement("1234567890123456789012345678901234567890");

        TextGroupElement ele15 = new TextGroupElement(new TextElement("同行不同格式1"), new TextElement("同行不同格式2").setSize(FontSize.SMALL));
        TextGroupElement ele16 = new TextGroupElement(new TextElement("同行不同格式1换行显示"), new TextElement("同行不同格式2换行显示").setBold(true).setSize(FontSize.LARGE));

        add(ele1)
                .add(ele3)
                .add(ele2)
                .add(ele4)
                .add(ele5)
                .add(ele6)
                .add(ele7)
                .add(ele8)
                .add(ele9)
                .add(ele10);
//        add(ele11)
//                .add(ele12)
//                .add(ele13);
//                .add(ele14)
        add(ele15);
        add(ele16);
        asynCreate(context, new Callback() {
            @Override
            public void onComplete(File file) {
                logger.warn("签购单创建完成：" + file.toString());
            }
        });
    }

    /**
     * 异步创建电子签购单的回调接口
     */
    public interface Callback {
        void onComplete(File file);
    }


}
