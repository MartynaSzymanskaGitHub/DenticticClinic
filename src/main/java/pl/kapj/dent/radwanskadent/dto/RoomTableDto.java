package pl.kapj.dent.radwanskadent.dto;

import java.time.LocalDateTime;

public class RoomTableDto {

    private Long id;
    private String name;
    private String description;
    private boolean underRenovation;
    private boolean currentlyUnderRenovation;
    private boolean plannedRenovation;
    private LocalDateTime renovationTimeStart;
    private LocalDateTime renovationTimeEnd;

    public RoomTableDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnderRenovation() {
        return underRenovation;
    }

    public void setUnderRenovation(boolean underRenovation) {
        this.underRenovation = underRenovation;
    }

    public boolean isCurrentlyUnderRenovation() {
        return currentlyUnderRenovation;
    }

    public void setCurrentlyUnderRenovation(boolean currentlyUnderRenovation) {
        this.currentlyUnderRenovation = currentlyUnderRenovation;
    }

    public boolean isPlannedRenovation() {
        return plannedRenovation;
    }

    public void setPlannedRenovation(boolean plannedRenovation) {
        this.plannedRenovation = plannedRenovation;
    }

    public LocalDateTime getRenovationTimeStart() {
        return renovationTimeStart;
    }

    public void setRenovationTimeStart(LocalDateTime renovationTimeStart) {
        this.renovationTimeStart = renovationTimeStart;
    }

    public LocalDateTime getRenovationTimeEnd() {
        return renovationTimeEnd;
    }

    public void setRenovationTimeEnd(LocalDateTime renovationTimeEnd) {
        this.renovationTimeEnd = renovationTimeEnd;
    }
}
