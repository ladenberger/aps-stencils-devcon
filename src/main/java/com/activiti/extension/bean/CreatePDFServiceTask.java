package com.activiti.extension.bean;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ladenberger.aps.stencils.devcon.Note;

@Component("createPDFServiceTask")
public class CreatePDFServiceTask {

	private static Logger LOG = LoggerFactory.getLogger(CreatePDFServiceTask.class);

	@Autowired
	private NoteService notesService;

	public void execute(ActivityExecution execution) throws IOException {

		// Get Notizen of process
		List<Note> notes = notesService.getNotizen(execution);

		LOG.debug("Create PDF for notes {}", notes);

		// Only upload if some notes exist in the given process
		if (notes != null && notes.size() > 0) {

			PDDocument notesPdf = notesService.createPdf(notes, "My process notes");
			notesService.toFile(notesPdf, "/Users/lukas/dev/AlfrescoDevCon/processNotes.pdf");
			notesPdf.close();

		}

	}

}
