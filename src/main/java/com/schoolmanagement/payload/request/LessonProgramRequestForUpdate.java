package com.schoolmanagement.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.schoolmanagement.entity.enums.Day;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LessonProgramRequestForUpdate {
    @NotNull(message="Please enter day")
    private Day day;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "US")
    @NotNull(message="Please enter start time")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "US")
    @NotNull(message="Please enter stop time")
    private LocalTime stopTime;

    @NotNull(message="Please select lesson")
    @Size(min=1, message ="Lesson must not be empty")
    private Set<Long> lessonIdList;

    @NotNull(message="Please enter education term")
    private Long educationTermId;

    @NotNull(message = "Please select student")
    @Size(min = 1, message = "Student must not empty")
    private List<Long> studentIdList;

    @NotNull(message = "Please select Teacher")
    @Size(min = 1, message = "Teacher must not empty")
    private List<Long> teacherIdList;
}
