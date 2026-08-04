import { Route, Switch } from 'wouter';
import { AuthProvider } from '@/context/AuthContext';

import { lazy, Suspense } from 'react';
import ProtectedRoute from '@/components/auth/ProtectedRoute';

const HomePage = lazy(() => import('@/pages/HomePage'));
const LoginPage = lazy(() => import('@/pages/auth/LoginPage'));
const RegisterPage = lazy(() => import('@/pages/auth/RegisterPage'));
const NotFound = lazy(() => import('@/pages/errors/NotFound'));

const AdminDashboard = lazy(() => import('@/pages/admin/AdminDashboard'));
const ManageExams = lazy(() => import('@/pages/admin/ManageExams'));
const ManageStudents = lazy(() => import('@/pages/admin/ManageStudents'));
const AddExam = lazy(() => import('@/pages/admin/AddExam'));
const EditExam = lazy(() => import('@/pages/admin/EditExam'));
const AddQuestion = lazy(() => import('@/pages/admin/AddQuestion'));
const EditQuestion = lazy(() => import('@/pages/admin/EditQuestion'));
const ManageQuestions = lazy(() => import('@/pages/admin/ManageQuestions'));
const ViewResults = lazy(() => import('@/pages/admin/ViewResults'));
const GradeSubmission = lazy(() => import('@/pages/admin/GradeSubmission'));
const LeaderboardExam = lazy(() => import('@/pages/admin/LeaderboardExam'));
const LeaderboardGlobal = lazy(() => import('@/pages/admin/LeaderboardGlobal'));

const StudentDashboard = lazy(() => import('@/pages/student/StudentDashboard'));
const ExamPage = lazy(() => import('@/pages/student/ExamPage'));
const MyResults = lazy(() => import('@/pages/student/MyResults'));
const Profile = lazy(() => import('@/pages/student/Profile'));
const ResultDetail = lazy(() => import('@/pages/student/ResultDetail'));
const ReviewExam = lazy(() => import('@/pages/student/ReviewExam'));
const ResultConfirmation = lazy(() => import('@/pages/student/ResultConfirmation'));
const Leaderboard = lazy(() => import('@/pages/student/Leaderboard'));

const base = import.meta.env.BASE_URL.replace(/\/$/, '');

function App() {
  return (
    <AuthProvider>
      <Suspense fallback={<div className="d-flex justify-content-center align-items-center vh-100"><div className="spinner-border text-primary" role="status"><span className="visually-hidden">Loading...</span></div></div>}>
        <Switch>
        <Route path={`${base}/`} component={HomePage} />
        <Route path={`${base}/login`} component={LoginPage} />
        <Route path={`${base}/register`} component={RegisterPage} />

        {/* Admin */}
        <Route path={`${base}/admin/dashboard`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/manage-exams`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><ManageExams /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/students`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><ManageStudents /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/exam/add`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><AddExam /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/exam/edit/:id`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><EditExam /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/exam/:id/question/add`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><AddQuestion /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/question/edit/:id`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><EditQuestion /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/exam/manage-questions/:id`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><ManageQuestions /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/results/:id`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><ViewResults /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/grade/:id`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><GradeSubmission /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/leaderboard/:id`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><LeaderboardExam /></ProtectedRoute>
        </Route>
        <Route path={`${base}/admin/leaderboard`}>
          <ProtectedRoute allowedRoles={['ADMIN']}><LeaderboardGlobal /></ProtectedRoute>
        </Route>

        {/* Student */}
        <Route path={`${base}/student/dashboard`}>
          <ProtectedRoute allowedRoles={['STUDENT', 'ADMIN']}><StudentDashboard /></ProtectedRoute>
        </Route>
        <Route path={`${base}/exam/:id`}>
          <ProtectedRoute allowedRoles={['STUDENT', 'ADMIN']}><ExamPage /></ProtectedRoute>
        </Route>
        <Route path={`${base}/student/my-results`}>
          <ProtectedRoute allowedRoles={['STUDENT', 'ADMIN']}><MyResults /></ProtectedRoute>
        </Route>
        <Route path={`${base}/student/profile`}>
          <ProtectedRoute allowedRoles={['STUDENT', 'ADMIN']}><Profile /></ProtectedRoute>
        </Route>
        <Route path={`${base}/student/result-detail/:id`}>
          <ProtectedRoute allowedRoles={['STUDENT', 'ADMIN']}><ResultDetail /></ProtectedRoute>
        </Route>
        <Route path={`${base}/student/review/:id`}>
          <ProtectedRoute allowedRoles={['STUDENT', 'ADMIN']}><ReviewExam /></ProtectedRoute>
        </Route>
        <Route path={`${base}/student/result`}>
          <ProtectedRoute allowedRoles={['STUDENT', 'ADMIN']}><ResultConfirmation /></ProtectedRoute>
        </Route>
        <Route path={`${base}/student/leaderboard/:id`}>
          <ProtectedRoute allowedRoles={['STUDENT', 'ADMIN']}><Leaderboard /></ProtectedRoute>
        </Route>

        <Route component={NotFound} />
        </Switch>
      </Suspense>
    </AuthProvider>
  );
}

export default App;