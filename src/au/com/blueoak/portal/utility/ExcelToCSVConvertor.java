package au.com.blueoak.portal.utility;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * 
 * Utility class used to convert XLS/XLSX files into CSV. <br>
 * <br>
 * <b>(c)2015 Blue Oak Solutions Pty Ltd. All rights reserved.<br>
 */
public class ExcelToCSVConvertor
{
    /** The logger instance. */
    private static final Log LOG = LogFactory.getLog(ExcelToCSVConvertor.class);

    /** The quote character to be used in the file */
    private static final Character QUOTE_CHARACTER = '"';

    /** The delimiter used for the CSV file. */
    private static final String COMMA_DELIMITER = ",";

    public static final String XLS = ".XLS";
    public static final String XLSX = ".XLSX";
    public static final String CSV = ".CSV";
    
    /**
     * Regex used to validate the string value. The string should be in the following format: <br>
     * HH:mm:ss<br>
     * H:m:ss<br>
     * The regex ':ss' is not mandatory.
     */
    private static final String TIME_REGEX = "\\d{1,2}:\\d{1,2}(:\\d{1,2}){0,1}";
    
    /**
     * Regex used to validate the string value. The string should be in the following format: <br>
     * dd/MM/yyy <br>
     * dd.MM.yyy <br>
     * dd\MM\yyy <br>
     * d/M/yyy <br>
     * d.M.yyy <br>
     * d\M\yyy <br>
     * <br>
     * And any combination of '\', '/' and '.' is possible
     */
    private static final String DATE_REGEX = "\\d{1,2}(\\.|\\\\|\\/)\\d{1,2}(\\.|\\\\|\\/)\\d{4}";

    
    /**
     * Pattern for the DATE_TIME string validation.
     */
    private static final Pattern DATE_AND_TIME_PATTERN = Pattern
        .compile(new StringBuilder("^").append(DATE_REGEX).append(" {1}").append(TIME_REGEX).append("$").toString());
    
    /**
     * Pattern for the TIME string validation.
     */
    private static final Pattern TIME_PATTERN = Pattern
        .compile(new StringBuilder("^").append(TIME_REGEX).append("$").toString());
    
    /**
     * Pattern for the DATE string validation.
     */
    private static final Pattern DATE_PATTERN = Pattern
        .compile(new StringBuilder("^").append(DATE_REGEX).append("$").toString());

    /**
     * This method performs the conversion from XLS/XLSX to CSV files. <br>
     * The conversion is performed for each sheet from the XLS/XLSX file. For each sheet a new CSV file is created. The
     * XLS/XLSX file name and it's sheet name will be appended into the newly created CSV file.<br>
     * (e.g. we have the following excel file with one sheet: "ExcelFileName.xls" --> "ExcelFileName.xls_SheetName.csv")
     * <br>
     * An exception will be thrown when the cells that are being processed are within a merged area.
     * 
     * @param exelFile
     *            The EXCEL file object to be converted in CSV.
     * @return A list of file object for each sheet from the EXCEL file. If the sheet is empty no FileObject will be
     *         created.
     * @throws InvalidFormatException
     *             When existing merged cells are in the range of the the cells that are being converted into csv rows
     *             or there is a problem with the EXCEL file.
     * @throws EncryptedDocumentException
     *             When there is a problem with the EXCEL file.
     * @throws IOException
     *             When there is a problem with the EXCEL or CSV file
     */
    public static List<FileObject> convertXLSToCSV(FileObject exelFile)
        throws InvalidFormatException, EncryptedDocumentException, IOException
    {
        List<FileObject> csvFileList = new ArrayList<FileObject>();
        try
        {
            Workbook wb = WorkbookFactory.create(exelFile.getContent().getInputStream());

            // For storing data into CSV files
            if(LOG.isInfoEnabled())
            {
                StringBuilder message = new StringBuilder("converting ").append(exelFile.getName().getBaseName())
                    .append(" EXCEL file (").append(wb.getNumberOfSheets()).append(" sheets) to CSV");
                LOG.info(message);
            }

            final StringBuilder errorMessages = new StringBuilder("");
            boolean atLeastOneSheetPresent = false;
            for(int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++)
            {
                atLeastOneSheetPresent = true;
                final Sheet currentSheet = wb.getSheetAt(sheetIndex);
                try
                {
                    processSheet(exelFile.getName(), currentSheet, csvFileList);
                }
                catch(Exception exception)
                {
                    if(LOG.isDebugEnabled())
                    {
                        StringBuilder message = new StringBuilder("An error occurred while processing the sheet \"");
                        message.append(currentSheet.getSheetName());
                        message.append("\" from excel file ");
                        message.append(exelFile.getName());
                        message.append("\"");
                        LOG.debug(message.toString());

                    }
                    if( !StringUtils.isBlank(errorMessages))
                    {
                        errorMessages.append(". ");
                    }
                    errorMessages.append(exception.getMessage());
                }
            }

            // if we had at least one sheet present in the excel file
            // AND the CSV file has not been generated
            // AND an exception has been thrown while processing the sheet
            // THEN throw the exception because the excel was not processed successfully.
            if(atLeastOneSheetPresent && csvFileList.isEmpty() && !StringUtils.isBlank(errorMessages))
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug(errorMessages.toString());
                }
                throw new InvalidFormatException(errorMessages.toString());
            }
        }
        catch(InvalidFormatException | EncryptedDocumentException | IOException e)
        {
            if(LOG.isErrorEnabled())
                LOG.error("Error occured while trying to convert the EXCEL file.", e);
            throw e;
        }

        if(LOG.isInfoEnabled())
        {
            StringBuilder message = new StringBuilder("EXCEL to CSV conversion finished. A total of ");
            message.append(csvFileList.size());
            message.append(" CSV files have been generated");
            LOG.info(message.toString());
        }
        return csvFileList;
    }

    /**
     * Process the <code>sheet</code> from the excel file <code>excelFileName</code> and add the generated CSV file to
     * the <code>csvFileList</code>.
     *
     * @param excelFileName
     * @param sheet
     * @param csvFileList
     * @return
     * @throws InvalidFormatException
     * @throws IOException
     * @throws FileSystemException
     */
    private static void processSheet(final FileName excelFileName, final Sheet sheet,
        final List<FileObject> csvFileList)
            throws InvalidFormatException, IOException, FileSystemException
    {
        if(LOG.isDebugEnabled())
            LOG.debug(new StringBuilder("converting sheet ").append(sheet.getSheetName()).toString());

        // calculate for the current sheet the regions where the cells are merged
        final List<CellRangeAddress> mergedCellsRegionList = new ArrayList<CellRangeAddress>();
        for(int j = 0; j < sheet.getNumMergedRegions(); j++)
        {
            mergedCellsRegionList.add(sheet.getMergedRegion(j));
        }

        // process all rows from the sheet
        final StringBuilder data = new StringBuilder("");
        Integer maxColumnNumber = 0;
        for(int rowNumber = 0; rowNumber <= sheet.getLastRowNum(); rowNumber++)
        {
            final Row currentRow = sheet.getRow(rowNumber);
            maxColumnNumber = processRow(sheet, currentRow, data, mergedCellsRegionList, maxColumnNumber);
        }

        // after all rows have been processed then create the CSV file for the current sheet only if there is some data
        // that has been converted.
        if( !StringUtils.isBlank(data.toString()))
        {
            createCSVFile(excelFileName, sheet, csvFileList, data);
        }
        else
        {
            if(LOG.isDebugEnabled())
            {
                LOG.debug(sheet.getSheetName() + " sheet is empty.");
            }
        }
    }

    /**
     * Creates the CSV file using the <code>data</code> converted from the EXCEL file.
     *
     * @param excelFileName
     * @param sheet
     * @param csvFileList
     * @param data
     * @throws IOException
     * @throws FileSystemException
     */
    private static void createCSVFile(final FileName excelFileName, final Sheet sheet,
        final List<FileObject> csvFileList, final StringBuilder data)
            throws IOException, FileSystemException
    {
        FileObject csvOutputFile = null;
        String parentFolderName = excelFileName.getParent().toString().replace("file:///", "");

        // construct the CSV file's
        StringBuilder newFileName = new StringBuilder("file:///");
        if(parentFolderName.startsWith("zip:"))
        { // the xls files originates from a zip
            File zipFile = new File(parentFolderName.replace("zip:", "").replace("!/", ""));
            parentFolderName = zipFile.getParent();
            // add to the file name the zip from which it originates
            newFileName.append(parentFolderName).append(File.separator).append(zipFile.getName()).append("_");
        }
        else
        { // the XLS files are outside of the ZIP file
            newFileName.append(parentFolderName).append(File.separator);
        }
        newFileName.append(excelFileName.getBaseName()).append("_");
        newFileName.append(sheet.getSheetName());
        newFileName.append(CSV);

        OutputStream outputStream = null;
        try
        {
            FileSystemManager fsManager = VFS.getManager();
            csvOutputFile = fsManager.resolveFile(newFileName.toString());
            csvOutputFile.createFile();

            outputStream = csvOutputFile.getContent().getOutputStream();
            outputStream.write(data.toString().getBytes());

            csvFileList.add(csvOutputFile);

            if(LOG.isDebugEnabled())
            {
                StringBuilder message = new StringBuilder(sheet.getSheetName());
                message.append(" sheet has been successfuly converted to CSV: ");
                message.append(newFileName);
                LOG.debug(message);
            }
        }
        catch(IOException e)
        {
            if(LOG.isErrorEnabled())
                LOG.error("An error occured while trying to write the CSV contents.", e);
            throw e;
        }
        finally
        {
            if(outputStream != null)
            {
                outputStream.flush();
                outputStream.close();
            }
            if(csvOutputFile != null)
                csvOutputFile.close();
        }
    }

    /**
     * Process the <code>row</code> and append the information from it to the <code>data</code>.<br>
     * In case the <code>mergedCellsRegionList</code> is within the <code>row</code> to be processed an exception is
     * thrown.<br>
     * It is required for the count of maximum number of columns to be specified.<br>
     * This counter is used to generate empty values (""). <br>
     * For eg: <br>
     * "value","value","","","","","" <strong> --> generated 5 empty values</strong> <br>
     * "value","value","value","","","","" <strong> --> generated 4 empty values</strong><br>
     * "value","value","value","value","value","value"<strong> --> generated no empty values</strong><br>
     * <br>
     * <strong> These values are generated only if the number of columns from the <code>row</code> is less than the
     * maximum number of columns. The number of generated empty values is equal to: <code>maxColumnNumber</code> -
     * <code>row.columnsCount</code><br>
     * <br>
     * If the current row has MORE columns than the <code>maxColumnNumber</code> then the NEW
     * <code>maxColumnNumber</code> is returned.<br>
     * If the current row has FEWER columns than the <code>maxColumnNumber</code> then the OLD
     * <code>maxColumnNumber</code> is returned. </strong>
     * 
     * @param sheet
     * @param row
     * @param data
     * @param mergedCellsRegionList
     * @param maxColumnNumber
     * @return
     * @throws InvalidFormatException
     */
    private static int processRow(final Sheet sheet, final Row row, final StringBuilder data,
        final List<CellRangeAddress> mergedCellsRegionList, int maxColumnNumber)
            throws InvalidFormatException
    {
        if(isRowEmpty(row))
        {
            data.append("\r\n");
        }
        else
        {
            // the data read from the EXCEL file and converted into CSV format.
            final StringBuilder currentRowData = new StringBuilder("");

            int currentRowColumnNumber = 0;
            for(int cellNumber = 0; cellNumber < row.getLastCellNum(); cellNumber++)
            {
                currentRowColumnNumber++;
                final Cell cell = row.getCell(cellNumber);
                if(cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK)
                {
                    // append an empty value
                    currentRowData.append(QUOTE_CHARACTER);
                    currentRowData.append(QUOTE_CHARACTER);
                }
                else
                {
                    for(CellRangeAddress region : mergedCellsRegionList)
                    {
                        // If the region does contain the cell you have just read from the row
                        if(region.isInRange(cell.getRowIndex(), cell.getColumnIndex()))
                        {
                            StringBuilder errorMessage = new StringBuilder(
                                "EXCEL file conversion has failed for sheet name \"");
                            errorMessage.append(sheet.getSheetName());
                            errorMessage.append("\". Encountered at least one merged cell. Merged cells region is ");
                            errorMessage.append(region.formatAsString());
                            if(LOG.isErrorEnabled())
                                LOG.error(errorMessage.toString());

                            throw new InvalidFormatException(errorMessage.toString());
                        }
                    }

                    if(Cell.CELL_TYPE_NUMERIC == cell.getCellType())
                    {
                        if(HSSFDateUtil.isCellDateFormatted(cell))
                        {
                            currentRowData.append(QUOTE_CHARACTER);
                            currentRowData.append(getDateFormattedCellAsString(DateUtil.getJavaCalendarUTC(cell.getNumericCellValue(), false).getTime()));
                            currentRowData.append(QUOTE_CHARACTER);
                        }
                        else
                        {
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            currentRowData.append(QUOTE_CHARACTER);
                            try
                            {
                                currentRowData.append(new BigDecimal(cell.getStringCellValue()));
                            }
                            catch(NumberFormatException ex)
                            {
                                currentRowData.append(cell.getStringCellValue());
                            }
                            currentRowData.append(QUOTE_CHARACTER);
                        }
                    }
                    else
                    {
                        String cellValue = cell.getStringCellValue();
                        final boolean dateOnlyFormat = isStringAsDateFormat(cellValue);
                        boolean timeOnlyFormat = false;
                        boolean dateTimeFormat = false;
                        if( !dateOnlyFormat)
                        {
                            timeOnlyFormat = isStringAsTimeFormat(cellValue);
                            if( !timeOnlyFormat)
                            {
                                dateTimeFormat = isStringAsDateTimeFormat(cellValue);
                            }
                        }

                        if(dateOnlyFormat || timeOnlyFormat || dateTimeFormat)
                        {
                            // at this point we are sure that the string is actually a date
                            // in case of DATE-only or DATE_TIME format we have to replace any potential dots with "/"
                            cellValue = cellValue.replaceAll("\\.", "/");
                            cellValue = cellValue.replaceAll("\\\\", "/");
                            if(cellValue.split(":").length == 2)
                            {
                                // 01/01/2015 00:00 + :00
                                cellValue = cellValue + ":00";
                            }

                            currentRowData.append(QUOTE_CHARACTER);
                            try
                            {
                                SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.DATE_FORMAT);
                                dateFormat.setTimeZone(DateUtils.UTC_TIME_ZONE);
                                dateFormat.setLenient(true);
                                if(timeOnlyFormat)
                                {
                                    dateFormat.applyPattern(DateUtils.TIME_FORMAT);
                                }
                                else if(dateTimeFormat)
                                {
                                    dateFormat.applyPattern(DateUtils.DATE_AND_TIME_FORMAT);
                                }

                                currentRowData.append(dateFormat.format(dateFormat.parse(cellValue)));
                                dateFormat = null;
                            }
                            catch(ParseException e)
                            {
                                currentRowData.append(cellValue);

                                if(LOG.isDebugEnabled())
                                {
                                    LOG.debug(
                                        "Error occured parsing date. The original String (after replacing any \".\" or \"\\\") has been written to CSV and the execution has continued",
                                        e);
                                }
                            }
                            currentRowData.append(QUOTE_CHARACTER);
                        }
                        else
                        {
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            currentRowData.append(QUOTE_CHARACTER);
                            currentRowData.append(cellValue);
                            currentRowData.append(QUOTE_CHARACTER);
                        }
                    }
                }
                if(cellNumber < (row.getLastCellNum() - 1))
                {
                    currentRowData.append(COMMA_DELIMITER);
                }
            }

            if(currentRowColumnNumber > maxColumnNumber)
            {
                maxColumnNumber += (currentRowColumnNumber - maxColumnNumber);
            }

            int currentRowSize = currentRowData.toString().split(COMMA_DELIMITER).length;
            if(maxColumnNumber > currentRowSize)
            {
                while(maxColumnNumber > currentRowSize)
                {
                    currentRowData.append(COMMA_DELIMITER);
                    currentRowData.append(QUOTE_CHARACTER);
                    currentRowData.append(QUOTE_CHARACTER);
                    currentRowSize++;
                }
            }

            currentRowData.append("\r\n");
            data.append(currentRowData.toString());
        }
        return maxColumnNumber;
    }

    /**
     * Verify using the following regex {@link ExcelToCSVConvertor#DATE_REGEX} that the input string is formatted as a
     * DATE-only value.
     *
     * @param value
     * @return
     */
    private static boolean isStringAsDateFormat(String value)
    {
        return DATE_PATTERN.matcher(value).matches();
    }

    /**
     * Verify using the following regex {@link ExcelToCSVConvertor#TIME_REGEX} that the input string is formatted as a
     * TIME-only value.
     *
     * @param value
     * @return
     */
    private static boolean isStringAsTimeFormat(String value)
    {
        return TIME_PATTERN.matcher(value).matches();
    }
    
    /**
     * Verify using the following regex {@link ExcelToCSVConvertor#TIME_REGEX} and
     * {@link ExcelToCSVConvertor#DATE_REGEX} that the input string is formatted as a DATE_TIME value.
     *
     * @param value
     * @return
     */
    private static boolean isStringAsDateTimeFormat(String value)
    {
        return DATE_AND_TIME_PATTERN.matcher(value).matches();
    }

    /**
     * Verifies if the cell value contains only the time component, date component or date and time components.<br>
     * Based on this it returns a String value formatted as following:<br>
     * {@link DateUtils#DATE_FORMAT}<br>
     * {@link DateUtils#TIME_FORMAT}<br>
     * {@link DateUtils#DATE_AND_TIME_FORMAT}<br>
     * <br>
     * 
     * @param date
     * @return
     */
    private static String getDateFormattedCellAsString(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.DATE_FORMAT);
        dateFormat.setTimeZone(DateUtils.UTC_TIME_ZONE);
        
        // "TIME-only" values have date set to 31-Dec-1899 so if year is "31/12/1899" or "1900" you can assume it
        // is a "TIME-only" value
        final String dateStamp = dateFormat.format(date);
        if(dateStamp.endsWith("/1899") || dateStamp.endsWith("/1900"))
        {
            // Return "TIME-only" value as String HH:mm:ss
            dateFormat.applyPattern(DateUtils.TIME_FORMAT);
        }
        else
        {
            // here we can have a DATE-only or DATE_TIME value
            dateFormat.applyPattern(DateUtils.DATE_AND_TIME_FORMAT + ":SSS");
            final String timeStamp = dateFormat.format(date);
            if(timeStamp.endsWith("00:00:00:000"))
            {
                // if time is 00:00:00:000 you can assume it is a DATE-only value
                // WARNING!!! it could be midnight.
                // return the DATE-only field
                dateFormat.applyPattern(DateUtils.DATE_FORMAT);
            }
            else
            {
                // return DATE_TIME value as "dd/MMM/yyyy HH:mm:ss"
                dateFormat.applyPattern(DateUtils.DATE_AND_TIME_FORMAT);
            }
        }
        return dateFormat.format(date);
    }

    /**
     * Checks if the current row is empty. A row is empty when all the cells are blank.
     *
     * @param row
     * @return
     */
    private static boolean isRowEmpty(Row row)
    {
        boolean isEmpty = true;
        if(row != null)
        {
            for(int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++)
            {
                Cell cell = row.getCell(c);
                if(cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                {
                    isEmpty = false;
                    break;
                }
            }
        }
        else
        {
            LOG.debug("NULL row has been found");
        }

        return isEmpty;
    }
}