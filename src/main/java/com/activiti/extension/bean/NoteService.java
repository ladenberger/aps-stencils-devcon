package com.activiti.extension.bean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ladenberger.aps.stencils.devcon.Note;
import com.ladenberger.aps.stencils.devcon.ProcessVariables;

@Service
public class NoteService {

	private static Logger LOG = LoggerFactory.getLogger(NoteService.class);

	@Autowired
	private ProcessService processService;

	private ObjectMapper mapper = new ObjectMapper();

	private static PDFont FONT_TYPE_TEXT = PDType1Font.HELVETICA;

	private static PDFont FONT_TYPE_TEXT_BOLD = PDType1Font.HELVETICA_BOLD;

	private static float FONT_SIZE_TEXT = 12;

	private static float FONT_SIZE_TITLE = 14;

	private float TEXT_LINE_SIZE = 1.5f * FONT_SIZE_TEXT;

	private float TITLE_LINE_SIZE = 1.5f * FONT_SIZE_TITLE;

	/**
	 * Liefert eine Liste von {@link Note} Objekten fuer die uebergebene
	 * {@link ActivityExecution}.
	 * 
	 * @param execution
	 *            {@link ActivityExecution} Object
	 * @return eine Liste von {@link Note} Objekten
	 * @throws ActivitiException
	 *             falls beim Lesen der Variable
	 *             {@link AuszahlungProcessVariables#NOTES} ein Fehler
	 *             aufgetreten ist
	 */
	public List<Note> getNotizen(ActivityExecution execution) throws ActivitiException {
		return getNotizen(execution.getId());
	}

	/**
	 * Liefert eine Liste von {@link Note} Objekten fuer die
	 * {@code executionId}.
	 * 
	 * @param executionId
	 *            Execution ID
	 * @return eine Liste von {@link Note} Objekten
	 * @throws ActivitiException
	 *             falls beim Lesen der Variable
	 *             {@link AuszahlungProcessVariables#NOTES} ein Fehler
	 *             aufgetreten ist
	 */
	public List<Note> getNotizen(String executionId) throws ActivitiException {

		LOG.debug("Hole Notizen aus Auszahlung-Prozessinstanz");

		String notizenJsonString = processService.findProcessVariable(executionId, ProcessVariables.NOTES,
				String.class);

		List<Note> notizen;

		if (notizenJsonString == null) {
			LOG.debug("Es wurde keine '{}' Variable in der Prozessinstanz gefunden", ProcessVariables.NOTES);
			return null;
		} else {
			try {
				notizen = mapper.convertValue((ArrayNode) mapper.readTree(notizenJsonString),
						mapper.getTypeFactory().constructCollectionType(List.class, Note.class));
			} catch (IOException e) {
				throw new ActivitiException(
						"Es ist ein Fehler beim Lesen der Variable '" + ProcessVariables.NOTES + "' aufgetreten", e);
			}
		}

		LOG.debug("Notizen in Auszahlung-Prozessinstanz: {}", notizen);

		return notizen;

	}

	/**
	 * Fuegt eine neue {@link Note} zur Execution {@code executionId} hinzu.
	 * 
	 * @param executionId
	 *            Execution ID
	 * @param notiz
	 *            {@link Note} Objekt
	 * @throws ActivitiException
	 *             falls beim Schreiben der Variable
	 *             {@link AuszahlungProcessVariables#NOTES} ein Fehler
	 *             aufgetreten ist
	 */
	public void addNotiz(String executionId, Note notiz) throws ActivitiException {

		LOG.debug("Fuege Notiz {} zur Auszahlung-Prozessinstanz hinzu", notiz);

		List<Note> notizen = getNotizen(executionId);

		if (notizen == null) {
			notizen = new ArrayList<>();
		}

		notizen.add(notiz);

		try {
			processService.setProcessVariable(executionId, ProcessVariables.NOTES, mapper.writeValueAsString(notizen));
		} catch (IOException e) {
			throw new ActivitiException(
					"Es ist ein Fehler beim Schreiben der Variable '" + ProcessVariables.NOTES + "' aufgetreten", e);
		}

		LOG.debug("Neue Notizen in Auszahlung-Prozessinstanz: {}", notizen);

	}

	/**
	 * Erstellt eine {@link PDDocument} Objekt fuer die uebergebene Liste von
	 * {@link Note} Objekten und den Titel {@code title}.
	 * 
	 * @param notizen
	 *            Eine Liste von {@link Note} Objekten
	 * @param title
	 *            Ein belieber Titel der im oberen Bereich der PDF angezeigt
	 *            wird
	 * @return ein {@link PDDocument} Objekt
	 * @throws IOException
	 *             falls beim Erstellen der Notiz PDF ein Fehler aufgetreten ist
	 */
	public PDDocument createPdf(List<Note> notizen, String title) throws IOException {

		// Create a document and add a page to it
		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		PDPageContentStream contentStream = null;

		try {

			contentStream = new PDPageContentStream(document, page);

			// Start a new content stream which will "hold" the to be created
			// content

			PDRectangle mediabox = page.getMediaBox();
			float margin = 35;
			float width = mediabox.getWidth() - 2 * margin;
			float startX = mediabox.getLowerLeftX() + margin;
			float startY = mediabox.getUpperRightY() - margin;

			float yOffset = startY;

			contentStream.setFont(FONT_TYPE_TEXT_BOLD, FONT_SIZE_TITLE);

			// Render title
			contentStream.beginText();
			contentStream.newLineAtOffset(startX, startY);
			contentStream.showText(title);
			contentStream.endText();

			yOffset = yOffset - (TITLE_LINE_SIZE * 2);

			for (Note notiz : notizen) {

				List<String> contentLines = getLines(notiz.getContent(), width);
				String header = notiz.getFullName() + " - " + notiz.getDate();
				float lines = contentLines.size() + 2;

				float contentSize = TEXT_LINE_SIZE * lines;

				float reaminingSpace = yOffset - margin;

				if (contentSize >= reaminingSpace) {
					page = new PDPage();
					document.addPage(page);
					contentStream.close();
					contentStream = new PDPageContentStream(document, page);
					yOffset = startY;
				}

				renderNotiz(contentLines, header, contentStream, startX, yOffset);

				yOffset = yOffset - contentSize;

			}

		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		return document;

	}

	/**
	 * Hilfsmethode um das uebergebene {@link PDDocument} Objekt in ein byte
	 * array zu konvertieren.
	 * 
	 * @param document
	 *            Ein {@link PDDocument} Objekt
	 * @return Byte Array fuer das uebergebene {@link PDDocument} Objekt
	 * @throws IOException
	 *             falls bei der Konvertierung ein Fehler aufgetreten ist
	 */
	public byte[] toByteArray(PDDocument document) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		document.save(baos);

		byte[] byteArray;
		try {
			byteArray = baos.toByteArray();
		} finally {
			IOUtils.closeQuietly(baos);
		}

		return byteArray;

	}

	public void toFile(PDDocument document, String file) throws IOException {
		FileUtils.writeByteArrayToFile(new File(file), toByteArray(document));
	}

	private void renderNotiz(List<String> contentLines, String header, PDPageContentStream contentStream, float startX,
			float startY) throws IOException {

		contentStream.beginText();

		contentStream.newLineAtOffset(startX, startY);

		renderHeader(header, contentStream);
		renderNotizContent(contentLines, contentStream);

		contentStream.endText();

	}

	private void renderHeader(String header, PDPageContentStream contentStream) throws IOException {

		contentStream.setFont(FONT_TYPE_TEXT_BOLD, FONT_SIZE_TEXT);
		contentStream.showText(header);

	}

	private void renderNotizContent(List<String> lines, PDPageContentStream contentStream) throws IOException {

		contentStream.setFont(FONT_TYPE_TEXT, FONT_SIZE_TEXT);
		contentStream.newLineAtOffset(0, -TEXT_LINE_SIZE);
		for (String line : lines) {
			contentStream.showText(line);
			contentStream.newLineAtOffset(0, -TEXT_LINE_SIZE);
		}

	}

	private List<String> getLines(String text, float width) throws IOException {

		List<String> lines = new ArrayList<String>();
		int lastSpace = -1;
		while (text.length() > 0) {
			int spaceIndex = text.indexOf(' ', lastSpace + 1);
			if (spaceIndex < 0)
				spaceIndex = text.length();
			String subString = text.substring(0, spaceIndex);
			float size = FONT_SIZE_TEXT * FONT_TYPE_TEXT.getStringWidth(subString) / 1000;
			if (size > width) {
				if (lastSpace < 0)
					lastSpace = spaceIndex;
				subString = text.substring(0, lastSpace);
				lines.add(subString);
				text = text.substring(lastSpace).trim();
				lastSpace = -1;
			} else if (spaceIndex == text.length()) {
				lines.add(text);
				text = "";
			} else {
				lastSpace = spaceIndex;
			}
		}

		return lines;

	}

}
