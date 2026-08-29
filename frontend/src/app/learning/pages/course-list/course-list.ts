import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';
import { RouterLink } from '@angular/router';

import { Course } from '../../models/course.model';
import { CourseService } from '../../services/course.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-course-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './course-list.html',
  styleUrls: ['./course-list.css']
})
export class CourseList implements OnInit {

  private readonly courseService =
    inject(CourseService);

  private readonly changeDetector =
    inject(ChangeDetectorRef);

  private readonly authService = inject(AuthService);

  courses: Course[] = [];

  isLoading = true;

  errorMessage = '';

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.changeDetector.detectChanges();

    this.courseService
      .getAllCourses()
      .subscribe({
        next: (courses: Course[]) => {
          this.courses = this.authService.hasRole('LEARNER')
            ? courses.filter((course) => course.status === 'PUBLISHED')
            : courses;
          this.isLoading = false;
          this.errorMessage = '';

          this.changeDetector.detectChanges();
        },

        error: (error: unknown) => {
          console.error(
            'Failed to load courses:',
            error
          );

          this.courses = [];
          this.isLoading = false;

          this.errorMessage =
            'Courses could not be loaded. Please check that the learning management service is running.';

          this.changeDetector.detectChanges();
        }
      });
  }

  trackCourseById(
    index: number,
    course: Course
  ): string {
    return course.courseId;
  }

  getCoursePrice(
    course: Course
  ): string {
    if (
      course.pricingType === 'FREE' ||
      course.price === null ||
      course.price === undefined ||
      course.price === 0
    ) {
      return 'Free';
    }

    return `${course.currencyCode ?? 'INR'} ${course.price}`;
  }
}