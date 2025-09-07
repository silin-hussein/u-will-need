package at.htlleonding.vehicle.control;

import at.htlleonding.vehicle.entity.Image;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ImageRepository implements PanacheRepository<Image> {
}
