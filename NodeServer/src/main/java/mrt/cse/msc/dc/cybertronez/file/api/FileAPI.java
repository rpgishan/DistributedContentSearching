package mrt.cse.msc.dc.cybertronez.file.api;

import mrt.cse.msc.dc.cybertronez.file.FileDAO;
import mrt.cse.msc.dc.cybertronez.file.FileGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/fileAPI")
public class FileAPI {
    private static Logger LOG = LogManager.getLogger(FileAPI.class);

    @POST
    @Path("/retrieveFile")
    public Response getAppointment(FileDAO file) {

        String fileName = file.getName();
        int fileSize = file.getSize();
        LOG.info("Creating file with name " + fileName + " and with size " + fileSize);
        File responseFIle = FileGenerator.generate(fileSize, fileName);
        String contentDispositionHeader = "attachment; filename=\"" + fileName + "\"";
        return Response.status(200).entity(responseFIle).type("text/plain").header("Content-Disposition", contentDispositionHeader).build();
    }
}
