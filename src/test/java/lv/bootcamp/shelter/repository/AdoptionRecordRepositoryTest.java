package lv.bootcamp.shelter.repository;

import lv.bootcamp.shelter.model.AdoptionRecord;
import org.hibernate.exception.ConstraintViolationException;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AdoptionRecordRepositoryTest {

    @Autowired
    private AdoptionRecordRepository adoptionRecordRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldRejectMicrochipIdDuplicates() {
        Animal rex = new Animal(null,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                2,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        entityManager.persist(rex);
        entityManager.flush();

        Animal luna = new Animal(null,
                "Luna",
                AnimalType.CAT,
                "Persian",
                1,
                "Friendly",
                "123",
                AnimalStatus.AVAILABLE);

        assertThrows(ConstraintViolationException.class, () -> {
            entityManager.persist(luna);
            entityManager.flush();
        });
    }

    @Test
    void findRecentlyAdoptionsByAnimalType_shouldReturnFilteredAnimalsByTypeAndDate(){
        AdoptionRecord rec = new AdoptionRecord(null,
                new Animal(null,
                        "Luna",
                        AnimalType.CAT,
                        "Persian",
                        1,
                        "Friendly",
                        "124",
                        AnimalStatus.AVAILABLE),
                "daniils@gmail.com",
                LocalDateTime.from(LocalDateTime.now().minusDays(1))
                );
        AdoptionRecord rec1 = new AdoptionRecord(null,
                new Animal(null,
                        "Rex",
                        AnimalType.DOG,
                        "Labrador",
                        2,
                        "Friendly",
                        "123",
                        AnimalStatus.AVAILABLE),
                "sam@gmail.com",
                LocalDateTime.from(LocalDateTime.now().minusDays(2))
        );
        AdoptionRecord rec2 = new AdoptionRecord(null,
                new Animal(null,
                        "Mia",
                        AnimalType.DOG,
                        "Shiba Inu",
                        3,
                        "Friendly",
                        "111",
                        AnimalStatus.AVAILABLE),
                "nina@gmail.com",
                LocalDateTime.from(LocalDateTime.now().minusDays(4))
        );

        for (AdoptionRecord r : List.of(rec, rec1, rec2)) {
            entityManager.persist(r.getAnimal());
            entityManager.persist(r);
        }

        entityManager.flush();
        entityManager.clear();

        List<AdoptionRecord> filteredAnimals = adoptionRecordRepository.findRecentAdoptionsByAnimalType(AnimalType.DOG,
                LocalDateTime.from(LocalDateTime.now().minusDays(3)));
        assertThat(filteredAnimals).hasSize(1); // rex
        assertThat(filteredAnimals)
                .extracting(record -> record.getAnimal().getName())
                .containsExactly("Rex");

    }


}
