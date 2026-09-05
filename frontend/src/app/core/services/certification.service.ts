import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Certificate } from '../../learning/models/certification';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CertificationService {
  private readonly baseUrl = `${environment.apiUrl}/certificate`;

  private mockCertificates: Certificate[] = [
    // Srijita (ID: 1)
    {
      certid: 101,
      empid: 1,
      name: 'AWS Certified Solutions Architect - Professional',
      issuingOrganization: 'Amazon Web Services',
      issueDate: '2025-01-15',
      expiry: '2028-01-15',
      status: 'Valid'
    },
    {
      certid: 102,
      empid: 1,
      name: 'Spring Cloud Microservices Developer',
      issuingOrganization: 'VMware Tanzu',
      issueDate: '2024-06-20',
      expiry: '2027-06-20',
      status: 'Valid'
    },
    // Alex Vance (ID: 101)
    {
      certid: 201,
      empid: 101,
      name: 'Certified Kubernetes Administrator (CKA)',
      issuingOrganization: 'Cloud Native Computing Foundation',
      issueDate: '2024-03-10',
      expiry: '2027-03-10',
      status: 'Valid'
    },
    {
      certid: 202,
      empid: 101,
      name: 'HashiCorp Certified: Terraform Associate',
      issuingOrganization: 'HashiCorp',
      issueDate: '2024-08-15',
      expiry: '2026-08-15',
      status: 'Valid'
    },
    // Marcus Brodie (ID: 102)
    {
      certid: 301,
      empid: 102,
      name: 'TOGAF 9.2 Certified Enterprise Architect',
      issuingOrganization: 'The Open Group',
      issueDate: '2023-01-10',
      expiry: '2028-01-10',
      status: 'Valid'
    },
    // Sarah Jenkins (ID: 103)
    {
      certid: 401,
      empid: 103,
      name: 'ISTQB Advanced Level Test Automation Engineer',
      issuingOrganization: 'ISTQB',
      issueDate: '2024-05-12',
      expiry: '2027-05-12',
      status: 'Valid'
    },
    // John Smith (ID: 106)
    {
      certid: 501,
      empid: 106,
      name: 'Meta Certified Senior Front-End Developer',
      issuingOrganization: 'Meta / Coursera',
      issueDate: '2024-09-01',
      expiry: '2026-09-01',
      status: 'Valid'
    },
    // Jane Doe (ID: 107)
    {
      certid: 601,
      empid: 107,
      name: 'Google Cloud Professional Machine Learning Engineer',
      issuingOrganization: 'Google Cloud',
      issueDate: '2025-02-01',
      expiry: '2027-02-01',
      status: 'Valid'
    },
    // David Miller (ID: 108)
    {
      certid: 701,
      empid: 108,
      name: 'AWS Certified DevOps Engineer - Professional',
      issuingOrganization: 'Amazon Web Services',
      issueDate: '2024-11-10',
      expiry: '2027-11-10',
      status: 'Valid'
    },
    // Elena Rostova (ID: 109)
    {
      certid: 801,
      empid: 109,
      name: 'Certified Information Systems Security Professional (CISSP)',
      issuingOrganization: 'ISC2',
      issueDate: '2023-08-20',
      expiry: '2026-08-20',
      status: 'Valid'
    },
    // Michael Chang (ID: 110)
    {
      certid: 901,
      empid: 110,
      name: 'Oracle Certified Professional: Java SE 17 Developer',
      issuingOrganization: 'Oracle',
      issueDate: '2024-04-15',
      expiry: '2027-04-15',
      status: 'Valid'
    },
    // Priya Sharma (ID: 111)
    {
      certid: 1001,
      empid: 111,
      name: 'PostgreSQL Certified Database Administrator',
      issuingOrganization: 'PostgreSQL Professional',
      issueDate: '2024-10-05',
      expiry: '2026-10-05',
      status: 'Valid'
    }
  ];

  constructor(private http: HttpClient) {}

  private getExternalCertificates(): Certificate[] {
    try {
      const stored: any[] = JSON.parse(localStorage.getItem('ssn_external_certifications') || '[]');
      return stored.map(s => ({
        certid: typeof s.certificationId === 'number' ? s.certificationId : 2000 + (Math.abs(s.certificationName.length * 37) % 8000),
        empid: Number(s.employeeId),
        name: s.certificationName,
        issuingOrganization: s.issuingOrganization,
        issueDate: s.issueDate,
        expiry: s.expiryDate || '2028-12-31',
        status: s.verificationStatus === 'VERIFIED' ? 'Valid' : (s.verificationStatus === 'REJECTED' ? 'Revoked' : 'Pending Verification')
      }));
    } catch {
      return [];
    }
  }

  getAll(): Observable<Certificate[]> {
    const ext = this.getExternalCertificates();
    const base = [...this.mockCertificates, ...ext];
    if (environment.useMock) {
      return of(base);
    }
    return this.http.get<Certificate[]>(this.baseUrl).pipe(
      map(res => {
        if (!res || !Array.isArray(res) || res.length === 0) return base;
        const merged = [...res, ...ext];
        return merged;
      }),
      catchError(() => of(base))
    );
  }

  getById(id: number): Observable<Certificate> {
    const all = [...this.mockCertificates, ...this.getExternalCertificates()];
    const cert = all.find(c => c.certid === id) || all[0];
    if (environment.useMock) {
      return of({ ...cert });
    }
    return this.http.get<Certificate>(`${this.baseUrl}/${id}`).pipe(
      catchError(() => of({ ...cert }))
    );
  }

  getCertificatesByEmployee(empid: number): Observable<Certificate[]> {
    const matching = [...this.mockCertificates, ...this.getExternalCertificates()].filter(c => c.empid === empid);
    if (environment.useMock) {
      return of(matching);
    }
    return this.http.get<Certificate[]>(`${this.baseUrl}/employee/${empid}`).pipe(
      map(res => {
        if (!res || !Array.isArray(res)) return matching;
        const dynamicForEmp = this.getExternalCertificates().filter(c => c.empid === empid);
        return [...res, ...dynamicForEmp];
      }),
      catchError(() => of(matching))
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
