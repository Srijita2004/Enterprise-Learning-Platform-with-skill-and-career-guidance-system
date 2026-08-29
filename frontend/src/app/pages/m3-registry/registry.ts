import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { CertificationApiService } from '../../core/api/certification-api.service';
import { EmployeeDirectoryService } from '../../core/api/employee-directory.service';
import { apiErrorMessage } from '../../core/http-error';
import { ToastService } from '../../core/toast/toast.service';
import { Certification, CertificationStatus, ComplianceStatus, Employee, PagedResponse, VerificationStatus } from '../../models/certification.models';
import { humanizeStatus } from '../../shared/status-label';

@Component({selector:'app-registry-page',standalone:true,imports:[CommonModule,FormsModule,ReactiveFormsModule],templateUrl:'./registry.html',styleUrl:'./registry.css'})
export class RegistryPage {
  private readonly api=inject(CertificationApiService);private readonly employeesApi=inject(EmployeeDirectoryService);private readonly toast=inject(ToastService);private readonly fb=inject(FormBuilder);
  readonly result=signal<PagedResponse<Certification>|null>(null);readonly employees=signal<Employee[]>([]);readonly loading=signal(true);readonly saving=signal(false);readonly modalOpen=signal(false);readonly editing=signal<Certification|null>(null);readonly humanize=humanizeStatus;
  searchText='';status:CertificationStatus|''='';verification:VerificationStatus|''='';compliance:ComplianceStatus|''='';expiryBucket='';page=0;size=10;sort='expiryDate,asc';
  readonly form=this.fb.group({employeeId:[null as number|null,Validators.required],certificationName:['',Validators.required],issuingOrganization:['',Validators.required],credentialNumber:[''],issueDate:['',Validators.required],expiryDate:[''],warningWindowDays:[30,[Validators.required,Validators.min(1),Validators.max(365)]]});
  constructor(){this.loadEmployees();this.load();}
  load():void{this.loading.set(true);this.api.search({query:this.searchText,status:this.status,verification:this.verification,compliance:this.compliance,expiryBucket:this.expiryBucket,page:this.page,size:this.size,sort:this.sort}).pipe(finalize(()=>this.loading.set(false))).subscribe({next:r=>this.result.set(r),error:e=>this.toast.error(apiErrorMessage(e,'Unable to load certification registry.'))});}
  apply():void{this.page=0;this.load();} clear():void{this.searchText='';this.status='';this.verification='';this.compliance='';this.expiryBucket='';this.page=0;this.load();}
  go(delta:number):void{const r=this.result();if(!r)return;const next=this.page+delta;if(next>=0&&next<r.totalPages){this.page=next;this.load();}}
  openCreate():void{this.editing.set(null);this.form.reset({employeeId:null,certificationName:'',issuingOrganization:'',credentialNumber:'',issueDate:'',expiryDate:'',warningWindowDays:30});this.modalOpen.set(true);}
  openEdit(c:Certification):void{this.editing.set(c);this.form.reset({employeeId:c.employeeId,certificationName:c.certificationName,issuingOrganization:c.issuingOrganization,credentialNumber:c.credentialNumber||'',issueDate:c.issueDate,expiryDate:c.expiryDate||'',warningWindowDays:c.warningWindowDays});this.modalOpen.set(true);}
  save():void{if(this.form.invalid){this.form.markAllAsTouched();return;}const v=this.form.getRawValue();if(v.expiryDate&&v.issueDate&&v.expiryDate<v.issueDate){this.toast.error('Expiry date cannot be before issue date.');return;}this.saving.set(true);const edit=this.editing();const request={certificationName:v.certificationName!.trim(),issuingOrganization:v.issuingOrganization!.trim(),credentialNumber:v.credentialNumber?.trim()||null,issueDate:v.issueDate!,expiryDate:v.expiryDate||null,warningWindowDays:Number(v.warningWindowDays||30)};const call=edit?this.api.update(edit.certificationId,request):this.api.create({...request,employeeId:Number(v.employeeId)});call.pipe(finalize(()=>this.saving.set(false))).subscribe({next:()=>{this.toast.success(edit?'Certification updated.':'Certification registered.');this.modalOpen.set(false);this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Certification could not be saved.'))});}
  verify(c:Certification,status:VerificationStatus):void{this.api.updateVerification(c.certificationId,status).subscribe({next:()=>{this.toast.success(`Verification status set to ${humanizeStatus(status)}.`);this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Verification update failed.'))});}
  revoke(c:Certification):void{const reason=window.prompt(`Reason for revoking ${c.certificationName}:`);if(!reason?.trim())return;this.api.revoke(c.certificationId,reason.trim()).subscribe({next:()=>{this.toast.success('Certification revoked and retained in the audit trail.');this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Certification could not be revoked.'))});}
  syncM1():void{this.api.syncM1().subscribe({next:r=>{this.toast.success(`Enterprise sync complete: ${r.imported} imported, ${r.skippedExisting} already present, ${r.failed} failed.`);this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Enterprise certification sync failed.'))});}
  badge(v:string):string{return 'badge badge-'+v.toLowerCase().replaceAll('_','-');}
  private loadEmployees():void{this.employeesApi.employees().subscribe({next:r=>this.employees.set(r),error:e=>this.toast.error(apiErrorMessage(e,'Enterprise employee directory is unavailable.'))});}
}
