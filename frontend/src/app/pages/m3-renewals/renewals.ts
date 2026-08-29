import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { CertificationApiService } from '../../core/api/certification-api.service';
import { apiErrorMessage } from '../../core/http-error';
import { ToastService } from '../../core/toast/toast.service';
import { Certification, RenewalNotification, RenewalRequest } from '../../models/certification.models';
import { humanizeStatus } from '../../shared/status-label';

@Component({selector:'app-renewals-page',standalone:true,imports:[CommonModule,ReactiveFormsModule],templateUrl:'./renewals.html',styleUrl:'./renewals.css'})
export class RenewalsPage {
  private readonly api=inject(CertificationApiService);private readonly toast=inject(ToastService);private readonly fb=inject(FormBuilder);
  readonly renewals=signal<RenewalRequest[]>([]);readonly notifications=signal<RenewalNotification[]>([]);readonly certifications=signal<Certification[]>([]);readonly loading=signal(true);readonly modalOpen=signal(false);readonly saving=signal(false);readonly humanize=humanizeStatus;
  readonly form=this.fb.group({certificationId:['',Validators.required],proposedExpiryDate:['',Validators.required],justification:['',[Validators.required,Validators.minLength(5)]]});
  constructor(){this.load();}
  load():void{this.loading.set(true);forkJoin({renewals:this.api.renewals(),notifications:this.api.notifications(),certs:this.api.search({page:0,size:100,sort:'expiryDate,asc'})}).pipe(finalize(()=>this.loading.set(false))).subscribe({next:r=>{this.renewals.set(r.renewals);this.notifications.set(r.notifications);this.certifications.set(r.certs.content.filter(c=>c.active));},error:e=>this.toast.error(apiErrorMessage(e,'Unable to load renewal operations.'))});}
  openRequest():void{this.form.reset({certificationId:'',proposedExpiryDate:'',justification:''});this.modalOpen.set(true);}
  submitRequest():void{if(this.form.invalid){this.form.markAllAsTouched();return;}const v=this.form.getRawValue();this.saving.set(true);this.api.createRenewal(v.certificationId!,v.proposedExpiryDate!,v.justification!.trim()).pipe(finalize(()=>this.saving.set(false))).subscribe({next:()=>{this.toast.success('Renewal request submitted for approval.');this.modalOpen.set(false);this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Renewal request failed.'))});}
  approve(r:RenewalRequest):void{const note=window.prompt('Approval note (optional):')||'';this.api.approveRenewal(r.renewalRequestId,note).subscribe({next:()=>{this.toast.success('Renewal approved and the new expiry date was applied.');this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Renewal approval failed.'))});}
  reject(r:RenewalRequest):void{const note=window.prompt('Rejection reason:');if(!note?.trim())return;this.api.rejectRenewal(r.renewalRequestId,note.trim()).subscribe({next:()=>{this.toast.success('Renewal request rejected.');this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Renewal rejection failed.'))});}
  generate():void{this.api.generateNotifications().subscribe({next:r=>{this.toast.success(`${r.notificationsCreated} new notification(s) generated.`);this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Notification generation failed.'))});}
  acknowledge(n:RenewalNotification):void{this.api.acknowledgeNotification(n.notificationId).subscribe({next:()=>{this.toast.success('Notification acknowledged.');this.load();},error:e=>this.toast.error(apiErrorMessage(e,'Notification could not be acknowledged.'))});}
  badge(v:string):string{return 'badge badge-'+v.toLowerCase().replaceAll('_','-');}
}
