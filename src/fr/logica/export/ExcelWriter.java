package fr.logica.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import fr.logica.business.MessageUtils;
import fr.logica.business.context.RequestContext;
import fr.logica.business.data.ColumnData;
import fr.logica.business.data.ListData;
import fr.logica.db.DbQuery;
import fr.logica.db.DbQuery.Var;

/**
 * Utility class used to handle Excel export.
 */
public class ExcelWriter {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(ExcelWriter.class);

	/**
	 * Export query results to an Excel file.
	 * 
	 * @param file
	 *            Destination file.
	 * @param query
	 *            Executed query.
	 * @param data
	 *            Results to export.
	 * @throws IOException
	 *             Exception thrown is an error occurred during writing workbook or closing the file output stream.
	 * @see #writeFile(File, Workbook)
	 */
	public void export(File file, DbQuery query, ListData data, RequestContext ctx) throws IOException {
		MessageUtils msg = MessageUtils.getInstance(ctx);
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet(query.getName());

		/* Headers. */
		Row headerRow = sheet.createRow(0);
		Cell headerCell;
		int i = 0;
		int columnCount;

		for (Var v : query.getOutVars()) {
			ColumnData columnData = data.getColumns().get(v.tableId + "_" + v.name);
			if (!columnData.isVisible()) {
				continue;
			}
			String header = columnData.getTitle();

			if (header == null || "".equals(header)) {
				header = msg.getQryVarTitle(query.getName(), v.tableId, v.name);
			}
			headerCell = headerRow.createCell(i++);
			headerCell.setCellValue(header);
		}
		columnCount = i;

		/* Data. */
		Row row;
		Cell cell;
		i = 1;

		for (fr.logica.business.data.Row r : data.getRows()) {
			row = sheet.createRow(i++);
			int j = 0;

			for (Var var : query.getOutVars()) {
				ColumnData columnData = data.getColumns().get(var.tableId + "_" + var.name);

				if (!columnData.isVisible()) {
					continue;
				}
				cell = row.createCell(j++);
				Object value = r.get(var.tableId + "_" + var.name);

				if (value instanceof String) {
					cell.setCellValue((String) value);
				} else if (value instanceof Boolean) {
					cell.setCellValue((Boolean) value);
				} else if (value instanceof Number) {
					cell.setCellValue(((Number) value).doubleValue());
				}
			}
		}

		/* Columns are resized automatically, then a character is added to avoid problems (with false boolean for example). */
		for (i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
			if (sheet.getColumnWidth(i) <= (254 * 256))
				// max is 255 chars
				sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 256);
		}
		writeFile(file, workbook);
	}

	/**
	 * Write Excel workbook in the given file.
	 * 
	 * @param file
	 *            File in which workbook is written.
	 * @param workbook
	 *            Excel workbook to write.
	 * @throws IOException
	 *             Exception thrown is an error occurred during writing workbook or closing the file output stream.
	 */
	private void writeFile(File file, Workbook workbook) throws IOException {
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			workbook.write(fos);

		} catch (IOException exception) {
			LOGGER.error("Error while writing Excel workbook in the file : " + file.getAbsolutePath(), exception);
			throw exception;

		} finally {

			if (null != fos) {

				try {
					fos.close();

				} catch (IOException exception) {
					LOGGER.error("Error while closing output stream for file : " + file.getAbsolutePath(), exception);
					throw exception;
				}
			}
		}
	}

}
