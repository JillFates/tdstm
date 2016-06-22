package net.transitionmanager.utils;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by octavio on 6/21/16.
 */
public class ExcelDocumentConverter {
	private static Logger log = Logger.getLogger(ExcelDocumentConverter.class);

	public static HSSFWorkbook convertWorkbookToHSSF(Workbook source) {
		return (HSSFWorkbook) ExcelDocumentConverter.copyWorkbook(source, new HSSFWorkbook());
	}

	public static XSSFWorkbook convertWorkbookToXSSF(Workbook source) {
		return (XSSFWorkbook) ExcelDocumentConverter.copyWorkbook(source, new XSSFWorkbook());
	}

	public static Workbook copyWorkbook(Workbook source, Workbook destination) {
		for (int i = 0; i < source.getNumberOfSheets(); i++) {
			Sheet sourceSheet = source.getSheetAt(i);
			Sheet targetSheet = destination.createSheet(sourceSheet.getSheetName());
			copySheets(sourceSheet, targetSheet);
		}
		return destination;
	}

	public static void copySheets(Sheet source, Sheet destination) {
		copySheets(source, destination, true);
	}

	/**
	 * @param source the sheet to create from the copy.
	 * @param destination sheet to copy.
	 * @param copyStyle true copy the style.
	 */
	public static void copySheets(Sheet source, Sheet destination, boolean copyStyle) {
		int maxColumnNum = 0;
		Map<Integer, CellStyle> styleMap = (copyStyle) ? new HashMap<Integer, CellStyle>() : null;
		for (int i = source.getFirstRowNum(); i <= source.getLastRowNum(); i++) {
			Row srcRow = source.getRow(i);
			Row destRow = destination.createRow(i);
			if (srcRow != null) {
				copyRow(source, destination, srcRow, destRow, styleMap);
				if (srcRow.getLastCellNum() > maxColumnNum) {
					maxColumnNum = srcRow.getLastCellNum();
				}
			}
		}
		for (int i = 0; i <= maxColumnNum; i++) {
			destination.setColumnWidth(i, source.getColumnWidth(i));
		}
	}

	/**
	 * @param srcSheet
	 *            the sheet to copy.
	 * @param destSheet
	 *            the sheet to create.
	 * @param srcRow
	 *            the row to copy.
	 * @param destRow
	 *            the row to create.
	 * @param styleMap
	 *            -
	 */
	public static void copyRow(Sheet srcSheet, Sheet destSheet, Row srcRow, Row destRow,
														 Map<Integer, CellStyle> styleMap) {
		// manage a list of merged zone in order to not insert two times a
		// merged zone
		Set<CellRangeAddressWrapper> mergedRegions = new TreeSet<CellRangeAddressWrapper>();
		destRow.setHeight(srcRow.getHeight());
		// pour chaque row
		for (int j = srcRow.getFirstCellNum(); j >= 0 && j <= srcRow.getLastCellNum(); j++) {
			Cell oldCell = srcRow.getCell(j); // ancienne cell
			Cell newCell = destRow.getCell(j); // new cell
			if (oldCell != null) {
				if (newCell == null) {
					newCell = destRow.createCell(j);
				}
				// copy chaque cell
				copyCell(oldCell, newCell, styleMap);
				// copy les informations de fusion entre les cellules
				// System.out.println("row num: " + srcRow.getRowNum() +
				// " , col: " + (short)oldCell.getColumnIndex());
				CellRangeAddress mergedRegion = getMergedRegion(srcSheet, srcRow.getRowNum(),
						(short) oldCell.getColumnIndex());

				if (mergedRegion != null) {
					// System.out.println("Selected merged region: " +
					// mergedRegion.toString());
					CellRangeAddress newMergedRegion = new CellRangeAddress(mergedRegion.getFirstRow(),
							mergedRegion.getLastRow(), mergedRegion.getFirstColumn(), mergedRegion.getLastColumn());
					// System.out.println("New merged region: " +
					// newMergedRegion.toString());
					CellRangeAddressWrapper wrapper = new CellRangeAddressWrapper(newMergedRegion);
					if (isNewMergedRegion(wrapper, mergedRegions)) {
						mergedRegions.add(wrapper);
						destSheet.addMergedRegion(wrapper.range);
					}
				}
			}
		}

	}

	/**
	 * @param oldCell
	 * @param newCell
	 * @param styleMap
	 */
	public static void copyCell(Cell oldCell, Cell newCell, Map<Integer, CellStyle> styleMap) {
		if (styleMap != null) {
			//newCell.setCellComment(oldCell.getCellComment());
			Integer hash = oldCell.getCellStyle().hashCode();
			if (!styleMap.containsKey(hash)) {
				transform(styleMap, hash, oldCell, newCell);
			}
			newCell.setCellStyle(styleMap.get(hash));
		}

		switch (oldCell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				newCell.setCellValue(oldCell.getStringCellValue());
				break;
			case Cell.CELL_TYPE_NUMERIC:
				newCell.setCellValue(oldCell.getNumericCellValue());
				break;
			case Cell.CELL_TYPE_BLANK:
				newCell.setCellType(Cell.CELL_TYPE_BLANK);
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				newCell.setCellValue(oldCell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_ERROR:
				newCell.setCellErrorValue(oldCell.getErrorCellValue());
				break;
			case Cell.CELL_TYPE_FORMULA:
				newCell.setCellFormula(oldCell.getCellFormula());
				break;
			default:
				break;
		}

	}

	/**
	 * Copy Cell style from one to another
	 */
	private static void transform( Map<Integer, CellStyle> styleMap, Integer hash, Cell oldCell, Cell newCell) {
		CellStyle styleOld = oldCell.getCellStyle();
		CellStyle styleNew = newCell.getSheet().getWorkbook().createCellStyle();
		styleNew.setAlignment(styleOld.getAlignment());
		styleNew.setBorderBottom(styleOld.getBorderBottom());
		styleNew.setBorderLeft(styleOld.getBorderLeft());
		styleNew.setBorderRight(styleOld.getBorderRight());
		styleNew.setBorderTop(styleOld.getBorderTop());
		styleNew.setDataFormat(transform(styleOld.getDataFormat(), oldCell, newCell));
		styleNew.setFillBackgroundColor(styleOld.getFillBackgroundColor());
		styleNew.setFillForegroundColor(styleOld.getFillForegroundColor());
		styleNew.setFillPattern(styleOld.getFillPattern());
		styleNew.setFont(transform(oldCell.getSheet().getWorkbook().getFontAt(styleOld.getFontIndex()), newCell));
		styleNew.setHidden(styleOld.getHidden());
		styleNew.setIndention(styleOld.getIndention());
		styleNew.setLocked(styleOld.getLocked());
		styleNew.setVerticalAlignment(styleOld.getVerticalAlignment());
		styleNew.setWrapText(styleOld.getWrapText());
		styleMap.put(hash, styleNew);
	}

	private static short transform(short index, Cell oldCell, Cell newCell) {
		DataFormat formatOld = oldCell.getSheet().getWorkbook().createDataFormat();
		DataFormat formatNew = newCell.getSheet().getWorkbook().createDataFormat();
		return formatNew.getFormat(formatOld.getFormat(index));
	}

	private static Font transform(Font fontOld, Cell newCell) {
		Font fontNew = newCell.getSheet().getWorkbook().createFont();
		fontNew.setBoldweight(fontOld.getBoldweight());
		fontNew.setCharSet(fontOld.getCharSet());
		fontNew.setColor(fontOld.getColor());
		fontNew.setFontName(fontOld.getFontName());
		fontNew.setFontHeight(fontOld.getFontHeight());
		fontNew.setItalic(fontOld.getItalic());
		fontNew.setStrikeout(fontOld.getStrikeout());
		fontNew.setTypeOffset(fontOld.getTypeOffset());
		fontNew.setUnderline(fontOld.getUnderline());
		return fontNew;
	}

	/**
	 * Récupère les informations de fusion des cellules dans la sheet source
	 * pour les appliquer à la sheet destination... Récupère toutes les zones
	 * merged dans la sheet source et regarde pour chacune d'elle si elle se
	 * trouve dans la current row que nous traitons. Si oui, retourne l'objet
	 * CellRangeAddress.
	 *
	 * @param sheet
	 *            the sheet containing the data.
	 * @param rowNum
	 *            the num of the row to copy.
	 * @param cellNum
	 *            the num of the cell to copy.
	 * @return the CellRangeAddress created.
	 */
	public static CellRangeAddress getMergedRegion(Sheet sheet, int rowNum, short cellNum) {
		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress merged = sheet.getMergedRegion(i);
			if (merged.isInRange(rowNum, cellNum)) {
				return merged;
			}
		}
		return null;
	}

	/**
	 * Check that the merged region has been created in the destination sheet.
	 *
	 * @param newMergedRegion
	 *            the merged region to copy or not in the destination sheet.
	 * @param mergedRegions
	 *            the list containing all the merged region.
	 * @return true if the merged region is already in the list or not.
	 */
	private static boolean isNewMergedRegion(CellRangeAddressWrapper newMergedRegion,
																					 Set<CellRangeAddressWrapper> mergedRegions) {
		return !mergedRegions.contains(newMergedRegion);
	}
}
