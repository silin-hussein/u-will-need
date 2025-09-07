package at.htlleonding.vehicle.boundary;

import at.htlleonding.vehicle.control.ImageRepository;
import at.htlleonding.vehicle.control.InitBean;
import at.htlleonding.vehicle.control.VehicleRepository;
import at.htlleonding.vehicle.entity.Vehicle;
import at.htlleonding.vehicle.entity.VehicleMapper;
import at.htlleonding.vehicle.entity.dto.VehicleDto;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.quarkus.vertx.web.Param;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

@Path("/vehicle")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
public class VehicleResource {

    @Inject
    VehicleRepository vehicleRepository;

    @Inject
    ImageRepository imageRepository;

    @Inject
    VehicleMapper vehicleMapper;

    @Inject
    InitBean initBean;

    @GET
    public Response findAll() {
        return Response.ok(
                vehicleRepository.listAll(Sort.by("brand", "model"))
        ).build();
    }



    @Transactional
    @POST
    public Response save(@Context UriInfo uriInfo, Vehicle vehicle) {
        Log.info("save -> " + vehicle);

        // is the car existing?
        if (vehicle.getId() != null) {
            // checking, if a car with the given id exists
            var existingVehicle = vehicleRepository.findById(vehicle.getId());
            if (existingVehicle != null) {
                Log.info("save: vehicle is null");
                return Response
                        .notModified()
                        .header("reason", "this id already exists - use PATCH for updating")
                        .build();
            }
        }


        var v = vehicleRepository.getEntityManager().merge(vehicle);
        Log.info(v.toString());

        UriBuilder uriBuilder = uriInfo
                .getAbsolutePathBuilder()
                .path(v.getId().toString());
        return Response.created(uriBuilder.build()).build();


    }


    /**
     * Upload an image for a vehicle.
     */
    @POST
    @Path("{vehicleid}/image")
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(@PathParam("vehicleid") Long vehicleId,
                           @FormParam("file") FileUpload upload) {

        // Validate input
        if (upload == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"File is required\"}")
                    .build();
        }

        // Validate file size (e.g., 30MB limit)
        if (upload.size() > 30 * 1024 * 1024) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"File too large\"}")
                    .build();
        }

        // Validate file type
        String contentType = upload.contentType();
        if (!isValidImageType(contentType)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid file type\"}")
                    .build();
        }

        // Sanitize filename
        String sanitizedFilename = sanitizeFilename(upload.fileName());

        try (InputStream in = new FileInputStream(upload.uploadedFile().toFile())) {
            Long imageId = vehicleRepository.uploadImage(vehicleId, sanitizedFilename, in);
            return Response.ok().entity("{\"imageId\":\"" + imageId + "\"}").build();
        } catch (IOException e) {
            return Response.serverError().entity("Upload fehlgeschlagen").build();
        }


//        try (var in = upload.openStream()) {
//            String stored = vehicleRepository.uploadImage(id, upload.fileName(), in);
//            return Response.ok().entity("{\"imagePath\":\"" + stored + "\"}").build();
//        } catch (IOException e) {
//            return Response.serverError().entity("Upload fehlgeschlagen").build();
//        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif"));
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }



    @GET
    @Path("{vehicleId}/images/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVehicleImagesJson(@PathParam("vehicleId") Long vehicleId) {
        var images = imageRepository.list("vehicle.id", vehicleId);

        // nur die IDs zurückgeben
        List<Long> imageIds = images.stream()
                .map(img -> img.getId())
                .toList();

        return Response.ok(imageIds).build();
    }


    @GET
    @Path("/image")
    @Produces("image/jpeg")
    public Response getVehicleImageByImageId(
            @QueryParam("imageid") Long imageId
    ) {
        byte[] imageData = imageRepository.findById(imageId).getImageData();
        if (imageData == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(imageData).build();
    }
}

