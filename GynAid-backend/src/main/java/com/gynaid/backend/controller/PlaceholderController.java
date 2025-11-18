package com.gynaid.backend.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/placeholder")
public class PlaceholderController {

    @GetMapping("/{width}/{height}")
    public ResponseEntity<byte[]> getPlaceholder(
            @PathVariable int width,
            @PathVariable int height) throws IOException {
        
        // Create a simple colored placeholder image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        
        // Set background color (light gray)
        graphics.setColor(new Color(240, 240, 240));
        graphics.fillRect(0, 0, width, height);
        
        // Set border color (light blue)
        graphics.setColor(new Color(173, 216, 230));
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRect(1, 1, width-2, height-2);
        
        // Add text
        graphics.setColor(new Color(100, 100, 100));
        graphics.setFont(new Font("Arial", Font.PLAIN, Math.max(12, width/20)));
        
        String text = width + "x" + height;
        FontMetrics metrics = graphics.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        
        int textX = (width - textWidth) / 2;
        int textY = (height - textHeight) / 2 + metrics.getAscent();
        
        graphics.drawString(text, textX, textY);
        graphics.dispose();
        
        // Convert to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }
}