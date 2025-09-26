import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { MedicalHistoryResponse } from '../../../../models/medical-history.model';
import { MedicalHistoryDialog } from './medical-history-dialog';

const baseRecord: MedicalHistoryResponse = {
  id: 'record-1',
  userId: 'user-1',
  title: 'Diagnostic Visit',
  date: '2025-03-20',
  note: 'Follow-up in six months.',
  doctor: {
    doctorName: 'Jan',
    doctorSurname: 'Kowalski',
    userId: 'doctor-1',
    specializations: ['Cardiology'],
    valid: true,
  },
};

describe('MedicalHistoryDialog', () => {
  let fixture: ComponentFixture<MedicalHistoryDialog>;
  let component: MedicalHistoryDialog;
  let refSpy: jasmine.SpyObj<DynamicDialogRef>;

  function createComponent(mode: 'view' | 'edit') {
    TestBed.resetTestingModule();

    refSpy = jasmine.createSpyObj<DynamicDialogRef>('DynamicDialogRef', [
      'close',
    ]);

    TestBed.configureTestingModule({
      imports: [MedicalHistoryDialog],
      providers: [
        { provide: DynamicDialogRef, useValue: refSpy },
        {
          provide: DynamicDialogConfig,
          useValue: {
            data: {
              mode,
              record: baseRecord,
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MedicalHistoryDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should render in read-only mode without confirm action', () => {
    createComponent('view');

    const confirmButton = fixture.nativeElement.querySelector(
      '.dialog-actions__confirm',
    );

    expect(component).toBeTruthy();
    expect(confirmButton).toBeNull();
  });

  it('should emit updated record in edit mode', () => {
    createComponent('edit');

    component.form.controls.title.setValue('Updated Visit');
    component.form.controls.date.setValue(new Date('2025-04-10'));
    component.form.controls.doctorName.setValue('Anna');
    component.form.controls.doctorSurname.setValue('Nowak');
    component.form.controls.notes.setValue('Patient reported improvement.');

    component.submit();

    expect(refSpy.close).toHaveBeenCalledWith({
      mode: 'edit',
      record: jasmine.objectContaining({
        id: baseRecord.id,
        title: 'Updated Visit',
        date: '2025-04-10',
        doctor: jasmine.objectContaining({
          doctorName: 'Anna',
          doctorSurname: 'Nowak',
        }),
        note: 'Patient reported improvement.',
      }),
    });
  });
});
