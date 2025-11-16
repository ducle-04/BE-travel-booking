package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.DestinationDTO;
import com.travel.travelbooking.Entity.Region;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface DestinationService {

    DestinationDTO createDestination(DestinationDTO dto, MultipartFile imageFile) throws IOException;

    // READ
    List<DestinationDTO> getAllDestinations();
    DestinationDTO getDestinationById(Long id);
    List<DestinationDTO> searchDestinationsByName(String name);
    List<DestinationDTO> getDestinationsByRegion(Region region);

    // UPDATE
    DestinationDTO updateDestination(Long id, DestinationDTO dto, MultipartFile imageFile) throws IOException;

    // DELETE
    void deleteDestination(Long id);
}