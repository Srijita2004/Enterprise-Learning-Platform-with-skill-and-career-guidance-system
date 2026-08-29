export interface Certificate {
  certid: number; 
  empid: number; 
  name: string;
  issuingOrganization: string;
  issueDate: string; // ISO Date String
  expiry: string; // ISO Date String
  status: string; // Valid / Expired
}
