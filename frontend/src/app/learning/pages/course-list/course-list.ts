import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Course } from '../../models/course.model';
import { CourseService } from '../../services/course.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-course-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule
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
  searchQuery = '';
  selectedLevel = 'ALL';
  selectedCategory = 'ALL';

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

  get categories(): string[] {
    const set = new Set<string>();
    this.courses.forEach(c => {
      if (c.category) set.add(c.category);
    });
    return Array.from(set).sort();
  }

  get filteredCourses(): Course[] {
    const q = this.searchQuery.toLowerCase().trim();
    return this.courses.filter(course => {
      // Level filter
      if (this.selectedLevel !== 'ALL' && course.courseLevel !== this.selectedLevel) {
        return false;
      }
      // Category filter
      if (this.selectedCategory !== 'ALL' && course.category !== this.selectedCategory) {
        return false;
      }
      // Search query filter
      if (q) {
        const titleMatch = (course.title || '').toLowerCase().includes(q);
        const codeMatch = (course.courseCode || '').toLowerCase().includes(q);
        const categoryMatch = (course.category || '').toLowerCase().includes(q);
        const descMatch = (course.description || '').toLowerCase().includes(q);
        const levelMatch = (course.courseLevel || '').toLowerCase().includes(q);
        if (!titleMatch && !codeMatch && !categoryMatch && !descMatch && !levelMatch) {
          return false;
        }
      }
      return true;
    });
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.selectedLevel = 'ALL';
    this.selectedCategory = 'ALL';
    this.changeDetector.detectChanges();
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