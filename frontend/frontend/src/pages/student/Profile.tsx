import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { useAuth } from '@/context/AuthContext';
import { getStudentProfile, updateProfile, changePassword, uploadProfilePicture } from '@/services/studentService';

export default function Profile() {
  const { user, updateUser } = useAuth();
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const [profileForm, setProfileForm] = useState({ fullName: '', email: '', mobile: '' });
  const [pwForm, setPwForm] = useState({ current: '', newPw: '', confirm: '' });
  const [profileSuccess, setProfileSuccess] = useState(false);
  const [pwSuccess, setPwSuccess] = useState(false);
  const [pwError, setPwError] = useState('');
  const [profileError, setProfileError] = useState('');
  const [saveLoading, setSaveLoading] = useState(false);
  const [pwLoading, setPwLoading] = useState(false);
  const [photoLoading, setPhotoLoading] = useState(false);
  const [photoError, setPhotoError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getStudentProfile()
      .then((data) => {
        if (!cancelled) {
          setProfile(data);
          setProfileForm({
            fullName: data.fullName ?? '',
            email: data.email ?? '',
            mobile: data.mobileNumber ?? '',
          });
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, []);

async function handleProfileSave(e: React.FormEvent) {
     e.preventDefault();
     setProfileError('');
     setSaveLoading(true);
     try {
       const res = await updateProfile({
         fullName: profileForm.fullName,
         mobileNumber: profileForm.mobile,
       });
       if ((res as any).success) {
         setProfileSuccess(true);
         setTimeout(() => setProfileSuccess(false), 2000);
       } else {
         setProfileError((res as any).message || 'Failed to update profile.');
       }
     } catch {
       setProfileError('Failed to update profile.');
     } finally {
       setSaveLoading(false);
     }
   }

   async function handlePwChange(e: React.FormEvent) {
     e.preventDefault();
     setPwError('');
     setPwSuccess(false);
     if (pwForm.newPw !== pwForm.confirm) {
       setPwError('New passwords do not match.');
       return;
     }
     setPwLoading(true);
     try {
       const res = await changePassword({
         oldPassword: pwForm.current,
         newPassword: pwForm.newPw,
         confirmPassword: pwForm.confirm,
       });
       if ((res as any).success) {
         setPwSuccess(true);
         setPwForm({ current: '', newPw: '', confirm: '' });
         setTimeout(() => setPwSuccess(false), 2000);
       } else {
         setPwError((res as any).message || 'Failed to change password.');
       }
     } catch {
       setPwError('Failed to change password.');
     } finally {
       setPwLoading(false);
     }
   }

  async function handlePhotoChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setPhotoError('');
    setPhotoLoading(true);
    try {
      const res = await uploadProfilePicture(file);
      if (res.success) {
        setProfile((prev: any) => ({ ...prev, profilePicUrl: res.profilePicUrl }));
        updateUser({ profilePicUrl: res.profilePicUrl });
        setProfileSuccess(true);
        setTimeout(() => setProfileSuccess(false), 2000);
      } else {
        setPhotoError(res.message || 'Failed to upload photo.');
      }
    } catch {
      setPhotoError('Failed to upload photo.');
    } finally {
      setPhotoLoading(false);
      e.target.value = '';
    }
  }

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="text-center p-4">Loading profile…</div></div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="container mt-4" style={{ maxWidth: 760 }}>
        <h2 className="fw-bold mb-4">
          <i className="bi bi-person-circle text-primary me-2"></i>My Profile
        </h2>

        <div className="row g-4">
          {/* Profile picture card */}
          <div className="col-12">
            <div className="card shadow-sm">
              <div className="card-body d-flex align-items-center gap-4 p-4">
                {profile?.profilePicUrl ? (
                  <img
                    src={profile.profilePicUrl}
                    alt="Profile"
                    style={{ width: 80, height: 80, borderRadius: '50%', objectFit: 'cover', border: '2px solid #0d6efd' }}
                  />
                ) : (
                  <div
                    className="d-flex align-items-center justify-content-center text-white fw-bold"
                    style={{ width: 80, height: 80, borderRadius: '50%', background: 'linear-gradient(135deg, #0d6efd, #6f42c1)', fontSize: 28 }}
                  >
                    {(profile?.fullName || '').split(' ').map((w: string) => w[0]).join('').substring(0, 2).toUpperCase()}
                  </div>
                )}
                <div>
                  <h5 className="fw-bold mb-1">{profile?.fullName || user?.fullName}</h5>
                  <p className="text-muted mb-2">{profile?.username || user?.username}</p>
                  {photoError && <div className="text-danger small mb-2">{photoError}</div>}
                  <label className="btn btn-sm btn-outline-primary">
                    <i className="bi bi-camera me-1"></i>{photoLoading ? 'Uploading...' : 'Change Photo'}
                    <input type="file" className="d-none" accept="image/*" onChange={handlePhotoChange} disabled={photoLoading} />
                  </label>
                </div>
              </div>
            </div>
          </div>

          {/* Edit Details */}
          <div className="col-md-6">
            <div className="card shadow-sm h-100">
              <div className="card-header fw-semibold">
                <i className="bi bi-person-fill me-2"></i>Edit Details
              </div>
              <div className="card-body p-4">
                {profileSuccess && <div className="alert alert-success py-2">Profile updated!</div>}
                {profileError && <div className="alert alert-danger py-2">{profileError}</div>}
                <form onSubmit={handleProfileSave}>
                  <div className="mb-3">
                    <label className="form-label fw-semibold" htmlFor="fullName">Full Name</label>
                    <input
                      id="fullName"
                      name="fullName"
                      type="text"
                      className="form-control"
                      value={profileForm.fullName}
                      onChange={e => setProfileForm({ ...profileForm, fullName: e.target.value })}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold" htmlFor="email">Email</label>
                    <input
                      id="email"
                      name="email"
                      type="email"
                      className="form-control"
                      value={profileForm.email}
                      readOnly
                    />
                  </div>
                  <div className="mb-4">
                    <label className="form-label fw-semibold" htmlFor="mobile">Mobile Number</label>
                    <input
                      id="mobile"
                      name="mobile"
                      type="tel"
                      className="form-control"
                      value={profileForm.mobile}
                      onChange={e => setProfileForm({ ...profileForm, mobile: e.target.value })}
                    />
                  </div>
                  <button type="submit" className="btn btn-primary w-100">
                    <i className="bi bi-save me-2"></i>Save Changes
                  </button>
                </form>
              </div>
            </div>
          </div>

          {/* Change Password */}
          <div className="col-md-6">
            <div className="card shadow-sm h-100">
              <div className="card-header fw-semibold">
                <i className="bi bi-lock-fill me-2"></i>Change Password
              </div>
              <div className="card-body p-4">
                {pwSuccess && <div className="alert alert-success py-2">Password changed!</div>}
                {pwError && <div className="alert alert-danger py-2">{pwError}</div>}
                <form onSubmit={handlePwChange}>
                  <div className="mb-3">
                    <label className="form-label fw-semibold" htmlFor="currentPassword">Current Password</label>
                    <input
                      id="currentPassword"
                      name="oldPassword"
                      type="password"
                      className="form-control"
                      value={pwForm.current}
                      onChange={e => setPwForm({ ...pwForm, current: e.target.value })}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold" htmlFor="newPassword">New Password</label>
                    <input
                      id="newPassword"
                      name="newPassword"
                      type="password"
                      className="form-control"
                      value={pwForm.newPw}
                      onChange={e => setPwForm({ ...pwForm, newPw: e.target.value })}
                      required
                    />
                  </div>
                  <div className="mb-4">
                    <label className="form-label fw-semibold" htmlFor="confirmNewPassword">Confirm New Password</label>
                    <input
                      id="confirmNewPassword"
                      name="confirmPassword"
                      type="password"
                      className="form-control"
                      value={pwForm.confirm}
                      onChange={e => setPwForm({ ...pwForm, confirm: e.target.value })}
                      required
                    />
                  </div>
                  <button type="submit" className="btn btn-warning w-100">
                    <i className="bi bi-shield-lock me-2"></i>Update Password
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}