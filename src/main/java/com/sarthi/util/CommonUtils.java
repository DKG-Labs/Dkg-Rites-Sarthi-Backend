package com.sarthi.util;

import com.sarthi.constant.AppConstant;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.exception.InvalidInputException;
import com.sarthi.service.JwtService;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
//import org.springframework.mock.web.MockMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CommonUtils {
    @Autowired
    private static JwtService jwtService;

    public static Map<String, String> seperateNames(String fullName) {
        Map<String, String> map = new HashMap();
        if (fullName == null) {
            map.put("fname", "");
            map.put("lname", "");
            return map;
        }
        String[] arr = fullName.split(" ");
        if (arr.length == 1) {
            map.put("fname", arr[0]);
            map.put("lname", "");
        } else if (arr.length > 1) {
            map.put("fname", arr[0]);
            map.put("lname", arr[arr.length - 1]);
        }
        return map;
    }

    public static String returnOfNotNull(String value, String oldValue) {
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return oldValue;
    }
/*
    public static MultipartFile convertBase64ToMultipartFile(String base64String, String fileName) throws Exception {
        byte[] data = Base64.getDecoder().decode(base64String);
        String path = fileName + ".png";
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        outputStream.write(data);
        FileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(), file.getParentFile());
        fileItem.getOutputStream();
        if (file.exists()) {
            System.out.println("File Exist => " + file.getName() + " :: " + file.getAbsolutePath());
        }
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/image", input.readAllBytes());
        return multipartFile;


    }*/

    public static Date getDate(String value, String format) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            return simpleDateFormat.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Integer getYearsByDate(String date) {
        Date parse = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            parse = sdf.parse(date);
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                parse = sdf.parse(date);
            } catch (Exception ex) {

            }
        }
        if (parse == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(parse.getTime());
        Calendar crnt = Calendar.getInstance();
        int years = crnt.get(Calendar.YEAR) - c.get(Calendar.YEAR);
        return years;
    }

    public static BigDecimal getBigDecimal(String income) {
        BigDecimal incm = null;
        try {
            incm = new BigDecimal(income);
        } catch (Exception e) {

        }
        return incm;
    }

    public static String getDate(Date value, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(value);
    }

    public static String makeNullToBlank(String val) {
        if(val == null){
            val = "";
        }
        return val;
    }

    public static Long getLong(String val) {
        Long incm = null;
        try {
            incm = Long.parseLong(val);
        } catch (Exception e) {

        }
        return incm;
    }

   /* public static LocalDate convertStringToDateObject(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(dateString, formatter);
    }*/
   public static LocalDate convertStringToDateObject(String dateString) {
       if (dateString == null || dateString.trim().isEmpty()) {
           return null;
       }
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
       return LocalDate.parse(dateString, formatter);
   }
    public static LocalDate convertIsoDateStringToDateObject(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new DateTimeParseException("Invalid ISO date format: " + dateString, dateString, 0);
    }


    public static String convertDateToString(LocalDate date) {
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return date.format(formatter);
        }
        return null;  // Handle null case if necessary
    }

    public static String convertDateTooString(Date date) {
        if (date != null) {
            LocalDate localDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            return convertDateToString(localDate);
        }
        return null;
    }

    public static String saveBase64Image(String base64Image, String basePath) throws IOException {
        // Validate input
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return null; // Return null if the input is empty or null
        }

        try {
            // Extract Base64 data (remove metadata like "data:image/png;base64,")
            String[] parts = base64Image.split(",");
            String imageData = parts.length > 1 ? parts[1] : parts[0];

            // Decode Base64
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            // Check if the decoded byte array is empty (corrupted file)
            if (imageBytes.length == 0) {
                throw new IOException("Invalid or corrupted Base64 image data.");
            }

            // Determine file extension
            String fileExtension = getFileExtension(base64Image);

            // Generate a unique filename (timestamp + UUID)
            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + fileExtension;

            // Ensure the base path directory exists
            File directory = new File(basePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Define file path
            String filePath = basePath + File.separator + fileName;

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(imageBytes);
            }

            return fileName; // Return only the filename
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid Base64 format. Cannot decode.", e);
        }
    }

   /* private static String getFileExtension(String base64) {
        if (base64.startsWith("data:image/png")) return ".png";
        if (base64.startsWith("data:image/jpeg") || base64.startsWith("data:image/jpg")) return ".jpg";
        if (base64.startsWith("data:image/gif")) return ".gif";
        return ".jpg"; // Default to .jpg
    }*/
   private static String getFileExtension(String base64) {
       if (base64.startsWith("data:image/png")) return ".png";
       if (base64.startsWith("data:image/jpeg") || base64.startsWith("data:image/jpg")) return ".jpg";
       if (base64.startsWith("data:image/gif")) return ".gif";
       if (base64.startsWith("data:application/pdf")) return ".pdf";
       if (base64.startsWith("data:application/msword")) return ".doc";
       if (base64.startsWith("data:application/vnd.openxmlformats-officedocument.wordprocessingml.document")) return ".docx";
       if (base64.startsWith("data:application/vnd.ms-excel")) return ".xls";
       if (base64.startsWith("data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) return ".xlsx";
       if (base64.startsWith("data:text/plain")) return ".txt";
       return ".bin"; // fallback
   }

    public static String convertImageToBase64(String fileName, String basePath) throws IOException {
        // Construct the full file path
        String filePath = basePath + File.separator + fileName;

        // Ensure the file exists
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new IOException("File not found: " + filePath);
        }

        // Read file into byte array
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileBytes = fis.readAllBytes();

            // Encode bytes to Base64
            String base64String = Base64.getEncoder().encodeToString(fileBytes);

            // Get MIME type (file extension)
            String mimeType = getMimeType(fileName);

            // Return Base64 with proper metadata
            return "data:" + mimeType + ";base64," + base64String;
        }
    }

   private static String getMimeType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";

        return "image/jpeg"; // Default type
    }



    public static List<LocalDateTime> getDateRenge(String startDate, String endDate) {
        try{
            LocalDateTime start = CommonUtils.convertStringToDateObject(startDate).atStartOfDay();
            LocalDateTime end = CommonUtils.convertStringToDateObject(endDate).atTime(23, 59, 59);
            List<LocalDateTime> dateRenge = new ArrayList<>();
            dateRenge.add(start);
            dateRenge.add(end);
            return dateRenge;
        }
        catch(Exception e){
            throw new InvalidInputException(new ErrorDetails(
                AppConstant.ERROR_CODE_INVALID, 
                AppConstant.ERROR_TYPE_CODE_INVALID, 
                AppConstant.ERROR_TYPE_INVALID, 
                "Invalid start date and end date."));
        }
    }

    public static String convertSqlDateToString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(date);
    }

    public static LocalDate[] getPreviousQuarterRange() {
        LocalDate now = LocalDate.now();
        int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
        int prevQuarter = currentQuarter - 1;
        int year = now.getYear();
        if (prevQuarter == 0) {
            prevQuarter = 4;
            year -= 1;
        }

        int startMonth = (prevQuarter - 1) * 3 + 1;
        LocalDate start = LocalDate.of(year, startMonth, 1);
        LocalDate end = start.plusMonths(3).minusDays(1);

        return new LocalDate[]{start, end};
    }
    public static List<LocalDate> getDateRengeAsLocalDate(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<LocalDate> dateRange = new ArrayList<>();
        try {
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            dateRange.add(start);
            dateRange.add(end);
        } catch (Exception e) {
            e.printStackTrace(); // Handle parse exceptions
        }
        return dateRange;
    }

    public static String getUserIdFromAuthHeader(String authHeader){
        if((authHeader == null) || (!authHeader.startsWith("Bearer "))){
            throw new InvalidInputException(
                    new ErrorDetails(AppConstant.INVALID_TOKEN_CODE,
                            AppConstant.INVALID_TOKEN_TYPE,
                            AppConstant.ERROR_TYPE_ERROR,
                            "Invalid Token")
            );
        }
        String username = jwtService.extractUserId(authHeader.substring(7));
        return username;
    }

    public static LocalTime convertStringToTimeObject(String timeString) {

        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("HH:mm"); // 13:45

        return LocalTime.parse(timeString, formatter);
    }
}
