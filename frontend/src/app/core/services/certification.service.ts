import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Certificate } from '../../learning/models/certification';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CertificationService {
  private readonly baseUrl = `${environment.apiUrl}/certificate`;

  private mockCertificates: Certificate[] = [
    {
      certid: 1,
      empid: 101,
      name: 'Oracle Certified Professional: Java SE 17 Developer',
      issuingOrganization: 'Oracle',
      issueDate: '2025-01-15',
      expiry: '2028-01-15',
      status: 'Valid'
    },
    {
      certid: 2,
      empid: 101,
      name: 'Spring Certified Professional',
      issuingOrganization: 'VMware',
      issueDate: '2023-06-20',
      expiry: '2026-06-20',
      status: 'Valid'
    },
    {
      certid: 3,
      empid: 102,
      name: 'AWS Certified Solutions Architect - Professional',
      issuingOrganization: 'Amazon Web Services',
      issueDate: '2024-03-10',
      expiry: '2027-03-10',
      status: 'Valid'
    },
    {
      certid: 4,
      empid: 103,
      name: 'Certified ScrumMaster (CSM)',
      issuingOrganization: 'Scrum Alliance',
      issueDate: '2022-09-05',
      expiry: '2024-09-05',
      status: 'Expired'
    }
  ];

  constructor(private http: HttpClient) {}

  getAll(): Observable<Certificate[]> {
    if (environment.useMock) {
      return of([...this.mockCertificates]);
    }
    return this.http.get<Certificate[]>(this.baseUrl).pipe(
      catchError(this.handleError)
    );
  }

  getById(id: number): Observable<Certificate> {
    if (environment.useMock) {
      const cert = this.mockCertificates.find(c => c.certid === id);
      return cert ? of({ ...cert }) : throwError(() => new Error('Certificate not found'));
    }
    return this.http.get<Certificate>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getCertificatesByEmployee(empid: number): Observable<Certificate[]> {
    if (environment.useMock) {
      return of(this.mockCertificates.filter(c => c.empid === empid));
    }
    return this.http.get<Certificate[]>(`${this.baseUrl}/employee/${empid}`).pipe(
      catchError(this.handleError)
    );
  }

  create(certificate: Certificate): Observable<Certificate> {
    if (environment.useMock) {
      const newId =this.mockCertificates.length > 0 ? Math.max(...this.mockCertificates.map(c => c.certid ?? 0)) + 1: 1;
      
      // Calculate status based on current date
      const today = new Date();
      const expiryDate = new Date(certificate.expiry);
      const status = expiryDate > today ? 'Valid' : 'Expired';

      const newCert = { ...certificate, certid: newId, status };
      this.mockCertificates.push(newCert);
      return of(newCert);
    }
    return this.http.post<Certificate>(this.baseUrl, certificate).pipe(
      catchError(this.handleError)
    );
  }

  update(id: number, certificate: Certificate): Observable<Certificate> {
    if (environment.useMock) {
      const index = this.mockCertificates.findIndex(c => c.certid === id);
      if (index === -1) {
        return throwError(() => new Error('Certificate not found'));
      }
      
      const today = new Date();
      const expiryDate = new Date(certificate.expiry);
      const status = expiryDate > today ? 'Valid' : 'Expired';

      this.mockCertificates[index] = { ...certificate, certid: id, status };
      return of(this.mockCertificates[index]);
    }
    return this.http.put<Certificate>(`${this.baseUrl}/${id}`, certificate).pipe(
      catchError(this.handleError)
    );
  }

  delete(id: number): Observable<string> {
    if (environment.useMock) {
      const index = this.mockCertificates.findIndex(c => c.certid === id);
      if (index === -1) {
        return throwError(() => new Error('Certificate not found'));
      }
      this.mockCertificates.splice(index, 1);
      return of('Certificate deleted successfully');
    }
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' }).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: any) {
    console.error('API Error:', error);
    return throwError(() => new Error(error.message || 'Server error occurred'));
  }
}
