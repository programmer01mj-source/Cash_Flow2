const { z } = require('zod');

const signupSchema = z.object({
    name: z.string().min(2, "Please enter your full name (at least 2 characters)"),
    email: z.string().email("Please enter a valid email address"),
    password: z.string().min(6, "Password must be at least 6 characters long"),
    role: z.enum(["Customer", "Loan Officer", "Admin"]).optional()
});

const loginSchema = z.object({
    email: z.string().email("Please enter a valid email address"),
    role: z.enum(["Customer", "Loan Officer", "Admin"]).optional()
});

const loanApplySchema = z.object({
    amount: z.number().positive("Loan amount must be greater than zero").or(z.string().transform(val => parseFloat(val))),
    tenure: z.number().int().positive("Loan tenure must be a valid number of months").or(z.string().transform(val => parseInt(val, 10))),
    income: z.number().nonnegative("Monthly income must be zero or greater").or(z.string().transform(val => parseFloat(val))).optional(),
    nationalId: z.string().optional(),
    dob: z.string().optional()
});

const loanStatusSchema = z.object({
    status: z.enum(["Pending", "Under Review", "Approved", "Rejected", "Disbursed", "Paid", "Defaulted"])
});

const paymentSchema = z.object({
    amount: z.number().positive("Payment amount must be greater than zero").or(z.string().transform(val => parseFloat(val))),
    mode: z.enum(["Cash", "Banktransfer", "Tnm_Mpamba", "Airtel_money"]).optional()
});

function validate(schema) {
    return (req, res, next) => {
        try {
            req.body = schema.parse(req.body);
            next();
        } catch (error) {
            return res.status(400).json({
                error: 'Validation failed',
                details: error.errors ? error.errors.map(e => e.message) : error.message
            });
        }
    };
}

module.exports = {
    signupSchema,
    loginSchema,
    loanApplySchema,
    loanStatusSchema,
    paymentSchema,
    validate
};
