package ch.mvurdorf.platform.service;

import io.nayuki.qrcodegen.QrCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static io.nayuki.qrcodegen.QrCode.Ecc.MEDIUM;

@Slf4j
@Service
public class QRCodeService {

    public void generate(String text, ImageOutputStream out) throws IOException {
        var qrCode = QrCode.encodeText(text, MEDIUM);
        var qrImg = toImage(qrCode, 8, 1, 0xFFFFFF, 0x000000);
        ImageIO.write(qrImg, "png", out);
    }

    /**
     * @param scale  pixels per module
     * @param border the border width (according to scale)
     */
    public static BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
        var result = new BufferedImage((qr.size + (border * 2)) * scale,
                                       (qr.size + (border * 2)) * scale,
                                       BufferedImage.TYPE_INT_RGB);
        for (var y = 0; y < result.getHeight(); y++) {
            for (var x = 0; x < result.getWidth(); x++) {
                var dark = qr.getModule((x / scale) - border, (y / scale) - border);
                result.setRGB(x, y, dark ? darkColor : lightColor);
            }
        }
        return result;
    }

}
