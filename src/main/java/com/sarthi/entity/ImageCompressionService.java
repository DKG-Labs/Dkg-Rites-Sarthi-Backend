package com.sarthi.entity;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

@Service
public class ImageCompressionService {

    private static final int TARGET_SIZE = 20 * 1024;

    private static final int MAX_WIDTH = 1024;

}