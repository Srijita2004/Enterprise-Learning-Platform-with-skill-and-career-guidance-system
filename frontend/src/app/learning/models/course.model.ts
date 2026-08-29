export interface Course {
  courseId: string;
  courseCode: string;
  title: string;

  description?: string | null;
  category?: string | null;

  courseType?: string | null;
  courseLevel?: string | null;
  status: string;

  pricingType?: string | null;
  price?: number | null;
  currencyCode?: string | null;

  instructorName?: string | null;
  durationHours?: number | null;
  maxCapacity?: number | null;

  passingScore?: number | null;
  certificateEnabled?: boolean;

  startDate?: string | null;
  endDate?: string | null;

  averageRating?: number | null;
  ratingCount?: number | null;
  enrolledCount?: number | null;
  completedCount?: number | null;
  availableSeats?: number | null;
  completionRate?: number | null;

  createdAt?: string | null;
  updatedAt?: string | null;
}